(ns node-create
  (:require [conveyors.utils           :as utils]
            [clojure.test              :as cljtest]
            [conveyors.node.base       :as node-base]
            [conveyors.node.methods    :as node-methods]
            [conveyors.node.exceptions :as node-exceptions]
            [conveyors.node.properties :as node-properties]
            [conveyors.node.types      :as node-types]
            [conveyors.channel.base    :as channel-base]
            [conveyors.channel.properties :as channel-properties]
            [conveyors.channel.methods    :as channel-methods]))


(intern 'conveyors.channel.types 'types-list [::TestChannel1 ::TestChannel2 ::TestChannel3])
(intern 'conveyors.node.types    'types-list [::TestNode ::DerivedTestNode])

(channel-base/define-channel-type ::TestChannel1
                                  channel-properties/fields '(::h ::w))
(channel-base/define-channel-type ::TestChannel2
                                  channel-properties/fields '(::x ::y))
(channel-base/define-channel-type ::TestChannel3
                                  channel-properties/super  ::TestChannel1
                                  channel-properties/fields '(::c))

(def test-channel1 (channel-methods/create ::TestChannel1 ::h 1 ::w 2))
(def test-channel2 (channel-methods/create ::TestChannel2 ::x 3 ::y 4))
(def test-channel3 (channel-methods/create ::TestChannel3 ::h 4 ::w 6 ::c 10))

(node-base/define-node-type ::TestNode
                            node-properties/inputs  (list test-channel1 test-channel2)
                            node-properties/outputs (list test-channel3)
                            node-properties/func    (fn [x] (inc x))
                            node-properties/fields  '(::f1 ::f2 ::f3))
(node-base/define-node-type ::DerivedTestNode
                            node-properties/super   ::TestNode
                            node-properties/inputs  (list test-channel3 test-channel3)
                            node-properties/outputs (list test-channel1 test-channel2)
                            node-properties/func    (fn [x] (+ x 10))
                            node-properties/fields  '(::f4))


(cljtest/deftest node-creation
  (cljtest/testing "Node creation test"
    (let [test-node (node-methods/create ::TestNode ::f1 8 ::f2 "str" ::f3 ::arg)]
      (cljtest/is (=                  (node-methods/get-node-type    test-node) ::TestNode))
      (cljtest/is (=                  (node-methods/get-node-super   test-node) node-types/Node))
      (cljtest/is (utils/lists-equal? (node-methods/get-node-inputs  test-node) (list test-channel1 test-channel2)))
      (cljtest/is (utils/lists-equal? (node-methods/get-node-outputs test-node) (list test-channel3)))
      (cljtest/is (utils/lists-equal? (node-methods/get-node-fields  test-node) '(::f1 ::f2 ::f3)))

      (cljtest/is (= (node-methods/get-node-field test-node ::f1) 8))
      (cljtest/is (= (node-methods/get-node-field test-node ::f2) "str"))
      (cljtest/is (= (node-methods/get-node-field test-node ::f3) ::arg))

      (cljtest/is (= (node-methods/execute test-node   15)  16))
      (cljtest/is (= (node-methods/execute test-node -100) -99))
      (cljtest/is (= (node-methods/execute test-node    0)   1))

      (cljtest/is (= ((node-methods/get-node-func test-node)   15)  16))
      (cljtest/is (= ((node-methods/get-node-func test-node) -100) -99))
      (cljtest/is (= ((node-methods/get-node-func test-node)    0)   1)))))

(cljtest/deftest node-creation-derived
  (cljtest/testing "Derived node creation test"
    (let [derived-test-node (node-methods/create ::DerivedTestNode ::f1 8 ::f2 "str" ::f3 ::arg ::f4 '(1 2 3))]
      (cljtest/is (=                  (node-methods/get-node-type    derived-test-node) ::DerivedTestNode))
      (cljtest/is (=                  (node-methods/get-node-super   derived-test-node) ::TestNode))
      (cljtest/is (utils/lists-equal? (node-methods/get-node-inputs  derived-test-node) (list test-channel3 test-channel3)))
      (cljtest/is (utils/lists-equal? (node-methods/get-node-outputs derived-test-node) (list test-channel1 test-channel2)))
      (cljtest/is (utils/lists-equal? (node-methods/get-node-fields  derived-test-node) '(::f1 ::f2 ::f3 ::f4)))

      (cljtest/is (=                  (node-methods/get-node-field derived-test-node ::f1) 8))
      (cljtest/is (=                  (node-methods/get-node-field derived-test-node ::f2) "str"))
      (cljtest/is (=                  (node-methods/get-node-field derived-test-node ::f3) ::arg))
      (cljtest/is (utils/lists-equal? (node-methods/get-node-field derived-test-node ::f4) '(1 2 3)))

      (cljtest/is (= (node-methods/execute derived-test-node   15)  25))
      (cljtest/is (= (node-methods/execute derived-test-node -100) -90))
      (cljtest/is (= (node-methods/execute derived-test-node    0)  10))

      (cljtest/is (= ((node-methods/get-node-func derived-test-node)   15)  25))
      (cljtest/is (= ((node-methods/get-node-func derived-test-node) -100) -90))
      (cljtest/is (= ((node-methods/get-node-func derived-test-node)    0)  10)))))

(cljtest/deftest node-creation-duplicating-fields
  (cljtest/testing "Node with duplicating fields creation test"
    (cljtest/is
     (try
       (node-methods/create ::TestNode ::f1 8 ::f2 "str" ::f1 9)
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/create             (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/duplicating-fields (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))))

(cljtest/deftest node-creation-missing-fields
  (cljtest/testing "Node with missing fields creation test"
    (cljtest/is
     (try
       (node-methods/create ::TestNode ::f1 8 ::f2 "str")
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/create         (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/missing-fields (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))))

(cljtest/deftest node-creation-excess-fields
  (cljtest/testing "Node with excess fields creation test"
    (cljtest/is
     (try
       (node-methods/create ::TestNode ::f1 8 ::f2 "str" ::f3 ::arg ::f4 '(1 2 3))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/create        (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/excess-fields (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))))


(cljtest/run-tests 'node-create)