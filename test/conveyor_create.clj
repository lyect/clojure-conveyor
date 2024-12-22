(ns conveyor-create
  (:require [blocks.channel.methods                   :as channel-methods]
            [blocks.channel.types                     :as channel-types]
            [blocks.channel.definitions.image.fields  :as image-channel-fields]
            [blocks.channel.definitions.float.def     :as float-channel-def]
            [blocks.channel.definitions.number.fields :as number-channel-fields]
            [blocks.channel.definitions.png.def       :as png-channel-def]
            [blocks.channel.definitions.png.fields    :as png-channel-fields]
            [blocks.edge.methods                      :as edge-methods]
            [blocks.node.definitions.gamma.def        :as gamma-node-def]
            [blocks.node.definitions.image2bitmap.def :as image2bitmap-node-def]
            [blocks.node.definitions.image2image.def  :as image2image-node-def]
            [blocks.node.methods                      :as node-methods]
            [blocks.node.types                        :as node-types]
            [blocks.vertex.methods                    :as vertex-methods]
            [clojure.core.async                       :as a]
            [clojure.test                             :as cljtest]
            [conveyor.methods                         :as conveyor-methods]
            [utils]))

(png-channel-def/define-png-channel)
(float-channel-def/define-float-channel)
(image2bitmap-node-def/define-image2bitmap-node)
(image2image-node-def/define-image2image-node)
(gamma-node-def/define-gamma-node)

(defn- listen-outputs
  [conv-ref n_listens]
  (let [outputs (map (fn [[vertex-index output-index]]
                       (vertex-methods/get-vertex-output
                        (nth (conveyor-methods/get-conveyor-vertices conv-ref) vertex-index)))
                     (conveyor-methods/get-conveyor-outputs conv-ref))]
        (loop [counter 0]
          (when (< counter n_listens)
            (println (str "Listening... (" (+ counter 1) "/" n_listens ")"))
            (let [[[output-index value] output] (a/alts!! outputs)]
              (println (str "From channel " output-index ":"))
              (println (str "\tValue: " @value)))
            (recur (inc counter))))))

(cljtest/deftest conveyor-creation-simple
  (cljtest/testing "Simple conveyor creation test"
     (let [nodes             [(dosync (node-methods/create node-types/Image2BitmapT "PNG2Bitmap"))
                              (dosync (node-methods/create node-types/GammaT        "Gamma"))]
           edges             [(dosync (edge-methods/create 0 0 1 1))]
           conveyor          (dosync (conveyor-methods/create nodes edges))
           conveyor-vertices (conveyor-methods/get-conveyor-vertices conveyor)
           conveyor-edges    (conveyor-methods/get-conveyor-edges conveyor)
           conveyor-inputs   (conveyor-methods/get-conveyor-inputs conveyor)
           conveyor-outputs  (conveyor-methods/get-conveyor-outputs conveyor)
           image (dosync (channel-methods/create channel-types/PngT
                                                 image-channel-fields/width 200
                                                 image-channel-fields/height 350
                                                 png-channel-fields/alpha-used false))
           gamma (dosync (channel-methods/create channel-types/FloatT
                                                 number-channel-fields/value 10))]
       (println conveyor-inputs)
       (cljtest/is (= (count conveyor-vertices) 2))
       (cljtest/is (= (count @conveyor-edges) 1))
       (cljtest/is (utils/lists-equal? (conveyor-edges [0 0]) [1 1]))

       (cljtest/is (= (count conveyor-inputs) 2))
       (cljtest/is (utils/lists-equal? (nth conveyor-inputs 0) [0 0]))
       (cljtest/is (utils/lists-equal? (nth conveyor-inputs 1) [1 0]))

       (cljtest/is (= (count conveyor-outputs) 1))
       (cljtest/is (utils/lists-equal? (nth conveyor-outputs 0) [1 0]))

       (conveyor-methods/start conveyor {[0 0] image
                                         [1 0] gamma})
       
       (let [thread (Thread. (fn [] (listen-outputs conveyor 1)))]
         (.start thread)
         (.join thread)))))

(cljtest/deftest conveyor-with-selector-node
  (cljtest/testing "Create conveyor with selector node"
    (let [nodes             [(dosync (node-methods/create node-types/Image2BitmapT "PNG2Bitmap"))
                             (dosync (node-methods/create node-types/Image2ImageT  "Image2Image"))]
          edges             [(dosync (edge-methods/create 0 0 1 0))]
          conveyor          (dosync (conveyor-methods/create nodes edges))
          conveyor-vertices (conveyor-methods/get-conveyor-vertices conveyor)
          conveyor-edges    (conveyor-methods/get-conveyor-edges conveyor)
          conveyor-inputs   (conveyor-methods/get-conveyor-inputs conveyor)
          conveyor-outputs  (conveyor-methods/get-conveyor-outputs conveyor)
          image (dosync (channel-methods/create channel-types/PngT
                                                image-channel-fields/width 200
                                                image-channel-fields/height 350
                                                png-channel-fields/alpha-used false))]
      (cljtest/is (= (count conveyor-vertices) 2))
      (cljtest/is (= (count @conveyor-edges) 1))
      (cljtest/is (utils/lists-equal? (conveyor-edges [0 0]) [1 0]))

      (cljtest/is (= (count conveyor-inputs) 3))

      (cljtest/is (utils/lists-equal? (nth conveyor-inputs 0) [0 0]))
      (cljtest/is (utils/lists-equal? (nth conveyor-inputs 1) [1 1]))
      (cljtest/is (utils/lists-equal? (nth conveyor-inputs 2) [1 2]))

      (cljtest/is (= (count conveyor-outputs) 1))
      (cljtest/is (utils/lists-equal? (nth conveyor-outputs 0) [1 0]))

      (conveyor-methods/start conveyor {[0 0] image
                                        [1 1] conveyor-methods/ground-input
                                        [1 2] conveyor-methods/ground-input
                                        })

      (let [thread (Thread. (fn [] (listen-outputs conveyor 1)))]
        (.start thread)
        (.join thread)))))

(cljtest/run-tests 'conveyor-create)
