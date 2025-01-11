(ns conveyor-create
  (:require [blocks.channel.methods                   :as channel-methods]
            [blocks.channel.types                     :as channel-types]
            [blocks.channel.definitions.bitmap.def    :as bitmap-channel-def]
            [blocks.channel.definitions.float.def     :as float-channel-def]
            [blocks.channel.definitions.image.fields  :as image-channel-fields]
            [blocks.channel.definitions.integer.def   :as integer-channel-def]
            [blocks.channel.definitions.number.fields :as number-channel-fields]
            [blocks.channel.definitions.jpeg.def      :as jpeg-channel-def]
            [blocks.channel.definitions.jpeg.fields   :as jpeg-channel-fields]
            [blocks.channel.definitions.png.def       :as png-channel-def]
            [blocks.channel.definitions.png.fields    :as png-channel-fields]
            [blocks.edge.methods                      :as edge-methods]
            [blocks.node.definitions.concat.def       :as concat-node-def]
            [blocks.node.definitions.cut.def          :as cut-node-def]
            [blocks.node.definitions.cut.fields       :as cut-node-fields]
            [blocks.node.definitions.denoise.def      :as denoise-node-def]
            [blocks.node.definitions.difference.def   :as difference-node-def]
            [blocks.node.definitions.gamma.def        :as gamma-node-def]
            [blocks.node.definitions.image2image.def  :as image2image-node-def]
            [blocks.node.definitions.rgbsplit.def     :as rgbsplit-node-def]
            [blocks.node.methods                      :as node-methods]
            [blocks.node.types                        :as node-types]
            [clojure.test                             :as cljtest]
            [conveyor.methods                         :as conveyor-methods]
            [utils]))

(bitmap-channel-def/define)
(float-channel-def/define)
(integer-channel-def/define)
(jpeg-channel-def/define)
(png-channel-def/define)

(concat-node-def/define)
(cut-node-def/define)
(denoise-node-def/define)
(difference-node-def/define)
(gamma-node-def/define)
(image2image-node-def/define)
(rgbsplit-node-def/define)

(cljtest/deftest conveyor-creation-different-handlers
  (cljtest/testing "Conveyor creation test with different handlers"
     (let [nodes                    [(dosync (node-methods/create "Image2Image Transformer" node-types/Image2ImageT))
                                     (dosync (node-methods/create "Gamma Correction"        node-types/GammaT))]
           edges                    [(dosync (edge-methods/create 0 image2image-node-def/result-output
                                                                  1 gamma-node-def/image-input))]
           conveyor                 (dosync (conveyor-methods/create nodes edges))
           image1 (dosync (channel-methods/create channel-types/BitmapT
                                                  image-channel-fields/width 1
                                                  image-channel-fields/height 1))
           image2 (dosync (channel-methods/create channel-types/JpegT
                                                  image-channel-fields/width 2
                                                  image-channel-fields/height 2
                                                  jpeg-channel-fields/color-space "XYZ"
                                                  jpeg-channel-fields/chroma-subsampling-scheme "123"))
           image3 (dosync (channel-methods/create channel-types/PngT
                                                  image-channel-fields/width 3
                                                  image-channel-fields/height 3
                                                  png-channel-fields/alpha-used false))
           gamma1 (dosync (channel-methods/create channel-types/FloatT
                                                  number-channel-fields/value 1))
           gamma2 (dosync (channel-methods/create channel-types/FloatT
                                                  number-channel-fields/value 2))]
       
       (conveyor-methods/start conveyor)
       (conveyor-methods/store conveyor [[[0 image2image-node-def/to-jpeg-input] image1]
                                         [[0 image2image-node-def/to-png-input] image2]
                                         [[0 image2image-node-def/to-bitmap-input] image3]
                                         [[1 gamma-node-def/gamma-input] gamma1]
                                         [[1 gamma-node-def/gamma-input] gamma2]])

       (Thread/sleep 1000))))

(cljtest/deftest conveyor-creation-many-to-one
  (cljtest/testing "Conveyor creation test with many-to-one node"
    (let [nodes                    [(dosync (node-methods/create "Cutter"   node-types/CutT cut-node-fields/n 3))
                                    (dosync (node-methods/create "Denoiser" node-types/DenoiseT))]
          edges                    [(dosync (edge-methods/create 0 cut-node-def/results-output
                                                                 1 denoise-node-def/image-input))]
          conveyor                 (dosync (conveyor-methods/create nodes edges))
          image (dosync (channel-methods/create channel-types/BitmapT
                                                 image-channel-fields/width 1
                                                 image-channel-fields/height 1))]

      (conveyor-methods/start conveyor)
      (conveyor-methods/store conveyor [[[0 cut-node-def/image-input] image]])

      (Thread/sleep 1000))))

(cljtest/deftest conveyor-creation-complex
  (cljtest/testing "Complex conveyor creation test"
    (let [nodes                    [(dosync (node-methods/create "Concatter1"  node-types/ConcatT))
                                    (dosync (node-methods/create "RGBSplitter" node-types/RGBSplitT))
                                    (dosync (node-methods/create "Difference"  node-types/DifferenceT))
                                    (dosync (node-methods/create "Concatter2"  node-types/ConcatT))
                                    (dosync (node-methods/create "Gamma"       node-types/GammaT))]
          edges                    [(dosync (edge-methods/create 0 concat-node-def/result-output
                                                                 1 rgbsplit-node-def/image-input))
                                    (dosync (edge-methods/create 1 rgbsplit-node-def/red-output
                                                                 2 difference-node-def/l-image-input))
                                    (dosync (edge-methods/create 1 rgbsplit-node-def/green-output
                                                                 2 difference-node-def/r-image-input))
                                    (dosync (edge-methods/create 1 rgbsplit-node-def/blue-output
                                                                 3 concat-node-def/images-input))
                                    (dosync (edge-methods/create 2 difference-node-def/result-output
                                                                 3 concat-node-def/images-input))
                                    (dosync (edge-methods/create 3 concat-node-def/result-output
                                                                 4 gamma-node-def/image-input))]
          conveyor                 (dosync (conveyor-methods/create nodes edges))
          image1 (dosync (channel-methods/create channel-types/BitmapT
                                                 image-channel-fields/width 1
                                                 image-channel-fields/height 2))
          image2 (dosync (channel-methods/create channel-types/BitmapT
                                                 image-channel-fields/width 3
                                                 image-channel-fields/height 4))
          image3 (dosync (channel-methods/create channel-types/BitmapT
                                                 image-channel-fields/width 5
                                                 image-channel-fields/height 6))
          gamma  (dosync (channel-methods/create channel-types/FloatT
                                                 number-channel-fields/value 10))]

      (dosync (node-methods/set-required-input-load (nth nodes 0) concat-node-def/images-input 3))
      (dosync (node-methods/set-required-input-load (nth nodes 3) concat-node-def/images-input 2))

      (conveyor-methods/start conveyor)
      (conveyor-methods/store conveyor [[[0 concat-node-def/images-input] image1]
                                        [[0 concat-node-def/images-input] image2]
                                        [[0 concat-node-def/images-input] image3]
                                        [[4 gamma-node-def/gamma-input]   gamma]])

      (Thread/sleep 1000))))

(cljtest/run-tests 'conveyor-create)
