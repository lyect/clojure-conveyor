(ns conveyor-create
  (:require [clojure.test                           :as cljtest]
            [conveyor.methods                       :as conveyor-methods]
            [blocks.channel.methods                 :as channel-methods]
            [blocks.channel.types                   :as channel-types]
            [blocks.channel.definitions.image.fields :as image-channel-fields]
            [blocks.channel.definitions.integer.def  :as integer-channel-def]
            [blocks.channel.definitions.number.fields :as number-channel-fields]
            [blocks.channel.definitions.png.def     :as png-channel-def]
            [blocks.channel.definitions.png.fields  :as png-channel-fields]
            [blocks.edge.methods                    :as edge-methods]
            [blocks.node.definitions.gamma.def      :as gamma-node-def]
            [blocks.node.definitions.image2bitmap.def :as image2bitmap-node-def]
            [blocks.node.methods                    :as node-methods]
            [blocks.node.types                      :as node-types]
            [blocks.vertex.methods                  :as vertex-methods]
            [utils]))

(image2bitmap-node-def/define-image2bitmap-node)
(gamma-node-def/define-gamma-node)

(cljtest/deftest conveyor-creation-simple
  (cljtest/testing "Simple conveyor creation test"
    (dosync
     (let [nodes                [(node-methods/create node-types/Image2BitmapT "PNG2Bitmap")
                                 (node-methods/create node-types/GammaT      "Gamma")]
           edges                [(edge-methods/create 0 0 1 0)]
           conveyor             (conveyor-methods/create nodes edges)
           conveyor-vertices    (conveyor-methods/get-conveyor-vertices conveyor)
           vertex1              (nth conveyor-vertices 0)
           vertex2              (nth conveyor-vertices 1)

           conveyor-edges       (conveyor-methods/get-conveyor-edges conveyor)
           conveyor-inputs      (conveyor-methods/get-conveyor-inputs conveyor)
           conveyor-outputs     (conveyor-methods/get-conveyor-outputs conveyor)]

       (cljtest/is (= (count conveyor-vertices) 2))
       (cljtest/is (= (count @conveyor-edges) 1))
       (cljtest/is (utils/lists-equal? (conveyor-edges [0 0]) [1 0]))
       
       (cljtest/is (= (count conveyor-inputs) 2))
       (cljtest/is (utils/lists-equal? (nth conveyor-inputs 0) [0 0]))
       (cljtest/is (utils/lists-equal? (nth conveyor-inputs 1) [1 1]))

       (cljtest/is (= (count conveyor-outputs) 1))
       (cljtest/is (utils/lists-equal? (nth conveyor-outputs 0) [1 0]))

       (png-channel-def/define-png-channel)
       (integer-channel-def/define-integer-channel)
       (let [image (channel-methods/create channel-types/PngT
                                           image-channel-fields/width 200
                                           image-channel-fields/height 350
                                           png-channel-fields/alpha-used false)
             int (channel-methods/create channel-types/IntegerT
                                         number-channel-fields/value 10)]
         (conveyor-methods/start conveyor {[0 0] image
                                           [1 1] int}))))))

(cljtest/run-tests 'conveyor-create)
