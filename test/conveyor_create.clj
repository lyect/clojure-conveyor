(ns conveyor-create
  (:require [clojure.test                           :as cljtest]
            [conveyor.methods                       :as conveyor-methods]
            [blocks.edge.methods                    :as edge-methods]
            [blocks.node.definitions.gamma.def      :as gamma-node-def]
            [blocks.node.definitions.png2bitmap.def :as png2bitmap-node-def]
            [blocks.node.methods                    :as node-methods]
            [blocks.node.types                      :as node-types]
            [blocks.vertex.methods                  :as vertex-methods]
            [utils]))

(png2bitmap-node-def/define-png2bitmap-node)
(gamma-node-def/define-gamma-node)

(cljtest/deftest conveyor-creation-simple
  (cljtest/testing "Simple conveyor creation test"
    (dosync
     (let [nodes                [(node-methods/create node-types/Png2BitmapT "PNG2Bitmap")
                                 (node-methods/create node-types/GammaT      "Gamma")]
           edges                [(edge-methods/create 0 0 1 0)]
           conveyor             (conveyor-methods/create nodes edges)
           conveyor-vertices    (conveyor-methods/get-conveyor-vertices conveyor)
           vertex1              (nth conveyor-vertices 0)
           vertex2              (nth conveyor-vertices 1)
           vertex1-inputs-used  (vertex-methods/get-vertex-inputs-used  vertex1)
           vertex1-outputs-used (vertex-methods/get-vertex-outputs-used vertex1)
           vertex1-inputs-ready (vertex-methods/get-vertex-inputs-ready vertex1)
           vertex2-inputs-used  (vertex-methods/get-vertex-inputs-used  vertex2)
           vertex2-outputs-used (vertex-methods/get-vertex-outputs-used vertex2)
           vertex2-inputs-ready (vertex-methods/get-vertex-inputs-ready vertex2)
           conveyor-edges       (conveyor-methods/get-conveyor-edges conveyor)
           conveyor-inputs      (conveyor-methods/get-conveyor-inputs conveyor)
           conveyor-outputs     (conveyor-methods/get-conveyor-outputs conveyor)]
       (cljtest/is (utils/lists-equal? vertex1-inputs-used  [false]))
       (cljtest/is (utils/lists-equal? vertex1-outputs-used [true]))
       (cljtest/is (utils/lists-equal? vertex1-inputs-ready [false]))

       (cljtest/is (utils/lists-equal? vertex2-inputs-used  [true false]))
       (cljtest/is (utils/lists-equal? vertex2-outputs-used [false]))
       (cljtest/is (utils/lists-equal? vertex2-inputs-ready [false false]))

       (cljtest/is (= (count @conveyor-edges) 1))
       (cljtest/is (utils/lists-equal? (conveyor-edges [0 0]) [1 0]))
       
       (cljtest/is (= (count conveyor-inputs) 2))
       (cljtest/is (utils/lists-equal? (nth conveyor-inputs 0) [0 0]))
       (cljtest/is (utils/lists-equal? (nth conveyor-inputs 1) [1 1]))

       (cljtest/is (= (count conveyor-outputs) 1))
       (cljtest/is (utils/lists-equal? (nth conveyor-outputs 0) [1 0]))))))


(cljtest/run-tests 'conveyor-create)
