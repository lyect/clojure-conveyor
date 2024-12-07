(ns conveyors
  (:require
   [clojure.test              :as cljtest]
   [conveyors.node.base       :as node-base]
   [conveyors.node.methods    :as node-methods]
   [conveyors.node.channels   :as node-channels]
   [conveyors.node.properties :as node-properties]
   [conveyors.node.types      :as node-types]))

(node-base/define-node-type ::TestNode
                            node-properties/inputs    (list node-channels/INT node-channels/PNG)
                            node-properties/outputs   (list node-channels/PNG)
                            node-properties/func      (fn [x] (inc x))
                            node-properties/fields   '(::f1 ::f2 ::f3))

(cljtest/deftest node-creation
  (cljtest/testing "Node creation test"
    (let [test-node (node-methods/create ::TestNode ::f1 8 ::f2 "str" ::f3 ::arg)]
      (cljtest/is (= (node-methods/get-node-type    test-node) ::TestNode))
      (cljtest/is (= (node-methods/get-node-super   test-node) node-types/Node))
      (cljtest/is (= (node-methods/get-node-inputs  test-node) (list node-channels/INT node-channels/PNG)))
      (cljtest/is (= (node-methods/get-node-outputs test-node) (list node-channels/PNG)))
      (cljtest/is (= (node-methods/get-node-fields  test-node) '(::f1 ::f2 ::f3)))

      (cljtest/is (= (node-methods/get-node-field test-node ::f1) 8))
      (cljtest/is (= (node-methods/get-node-field test-node ::f2) "str"))
      (cljtest/is (= (node-methods/get-node-field test-node ::f3) ::arg))

      (cljtest/is (= (node-methods/execute test-node   15)  16))
      (cljtest/is (= (node-methods/execute test-node -100) -99))
      (cljtest/is (= (node-methods/execute test-node    0)   1))

      (cljtest/is (= ((node-methods/get-node-func test-node)   15)  16))
      (cljtest/is (= ((node-methods/get-node-func test-node) -100) -99))
      (cljtest/is (= ((node-methods/get-node-func test-node)    0)   1))
)))

(cljtest/deftest node-creation-renamed-fields
  (cljtest/testing "Node with renamed fields creation test"
    (cljtest/is (nil? (node-methods/create ::TestNode ::f1 8 ::f2 "str" ::f4 ::arg)))))

(cljtest/deftest node-creation-missing-fields
  (cljtest/testing "Node with missing fields creation test"
    (cljtest/is (nil? (node-methods/create ::TestNode ::f1 8 ::f2 "str")))))

(cljtest/deftest node-creation-excess-fields
  (cljtest/testing "Node with excess fields creation test"
    (cljtest/is (nil? (node-methods/create ::TestNode ::f1 8 ::f2 "str" ::f3 ::arg ::f4 "excess")))))

(cljtest/run-all-tests)