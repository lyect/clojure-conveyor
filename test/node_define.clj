(ns node-define
  (:require [clojure.test                 :as cljtest]
            [conveyors.channel.base       :as channel-base]
            [conveyors.channel.methods    :as channel-methods]
            [conveyors.channel.properties :as channel-properties]
            [conveyors.node.base          :as node-base]
            [conveyors.node.exceptions    :as node-exceptions]
            [conveyors.node.hierarchy     :as node-hierarchy]
            [conveyors.node.methods       :as node-methods]
            [conveyors.node.properties    :as node-properties]
            [conveyors.node.types         :as node-types]
            [conveyors.utils              :as utils]))


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


(cljtest/deftest node-define
  (cljtest/testing "Node definition test"
    (let [test-channel1 (channel-methods/create ::TestChannel1 ::h 1 ::w 2)
          test-channel2 (channel-methods/create ::TestChannel2 ::x 3 ::y 4)
          test-channel3 (channel-methods/create ::TestChannel3 ::h 4 ::w 6 ::c 10)]
      (node-base/define-node-type ::TestNode
        node-properties/inputs  (list test-channel1 test-channel2)
        node-properties/outputs (list test-channel3)
        node-properties/func    (fn [x] (inc x))
        node-properties/fields  '(::f1 ::f2 ::f3))
      (cljtest/is (node-methods/node-type-defined? ::TestNode))
      (let [test-node-type (node-hierarchy/tree ::TestNode)]
        (cljtest/is (=                  (test-node-type node-properties/T)       ::TestNode))
        (cljtest/is (=                  (test-node-type node-properties/super)   node-types/Node))
        (cljtest/is (utils/lists-equal? (test-node-type node-properties/inputs)  (list test-channel1 test-channel2)))
        (cljtest/is (utils/lists-equal? (test-node-type node-properties/outputs) (list test-channel3)))
        (cljtest/is (utils/lists-equal? (test-node-type node-properties/fields)  '(::f1 ::f2 ::f3))))
      (dosync (alter node-hierarchy/tree #(dissoc % ::TestNode)))
      (cljtest/is (and
                   (node-hierarchy/tree node-types/Node)
                   (= (count @node-hierarchy/tree) 1))))))

(cljtest/deftest node-define-derived
  (cljtest/testing "Derived node definition test"
    (let [test-channel1 (channel-methods/create ::TestChannel1 ::h 1 ::w 2)
          test-channel2 (channel-methods/create ::TestChannel2 ::x 3 ::y 4)
          test-channel3 (channel-methods/create ::TestChannel3 ::h 4 ::w 6 ::c 10)]
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
      (cljtest/is (node-methods/node-type-defined? ::DerivedTestNode))
      (let [derived-test-node-type (node-hierarchy/tree ::DerivedTestNode)]
        (cljtest/is (=                  (derived-test-node-type node-properties/T)       ::DerivedTestNode))
        (cljtest/is (=                  (derived-test-node-type node-properties/super)   ::TestNode))
        (cljtest/is (utils/lists-equal? (derived-test-node-type node-properties/inputs)  (list test-channel3 test-channel3)))
        (cljtest/is (utils/lists-equal? (derived-test-node-type node-properties/outputs) (list test-channel1 test-channel2)))
        (cljtest/is (utils/lists-equal? (derived-test-node-type node-properties/fields)  '(::f1 ::f2 ::f3 ::f4))))
      (dosync (alter node-hierarchy/tree #(dissoc % ::TestNode)))
      (dosync (alter node-hierarchy/tree #(dissoc % ::DerivedTestNode)))
      (cljtest/is (and
                   (node-hierarchy/tree node-types/Node)
                   (= (count @node-hierarchy/tree) 1))))))

(cljtest/deftest node-define-duplicated-fields
  (cljtest/testing "Node with duplicated fields definition test"
    (let [test-channel1 (channel-methods/create ::TestChannel1 ::h 1 ::w 2)
          test-channel2 (channel-methods/create ::TestChannel2 ::x 3 ::y 4)
          test-channel3 (channel-methods/create ::TestChannel3 ::h 4 ::w 6 ::c 10)]
      (cljtest/is
       (try
         (node-base/define-node-type ::TestNode
           node-properties/inputs  (list test-channel1 test-channel2)
           node-properties/outputs (list test-channel3)
           node-properties/func    (fn [x] (inc x))
           node-properties/fields  '(::f1 ::f2 ::f3 ::f2))
         (catch clojure.lang.ExceptionInfo e
           (if (and (= node-exceptions/define-node-type   (-> e ex-data node-exceptions/type-keyword))
                    (= node-exceptions/duplicating-fields (-> e ex-data node-exceptions/cause-keyword)))
             true
             false))))
      (cljtest/is (and
                   (node-hierarchy/tree node-types/Node)
                   (= (count @node-hierarchy/tree) 1))))))

(cljtest/deftest node-define-super-intersected-fields
  (cljtest/testing "Derived node with fields intersecting super's fields definition test"
    (let [test-channel1 (channel-methods/create ::TestChannel1 ::h 1 ::w 2)
          test-channel2 (channel-methods/create ::TestChannel2 ::x 3 ::y 4)
          test-channel3 (channel-methods/create ::TestChannel3 ::h 4 ::w 6 ::c 10)]
      (node-base/define-node-type ::TestNode
        node-properties/inputs  (list test-channel1 test-channel2)
        node-properties/outputs (list test-channel3)
        node-properties/func    (fn [x] (inc x))
        node-properties/fields  '(::f1 ::f2 ::f3))
      (cljtest/is
       (try
         (node-base/define-node-type ::DerivedTestNode
           node-properties/super   ::TestNode
           node-properties/inputs  (list test-channel3 test-channel3)
           node-properties/outputs (list test-channel1 test-channel2)
           node-properties/func    (fn [x] (+ x 10))
           node-properties/fields  '(::f1))
         (catch clojure.lang.ExceptionInfo e
           (if (and (= node-exceptions/define-node-type          (-> e ex-data node-exceptions/type-keyword))
                    (= node-exceptions/super-fields-intersection (-> e ex-data node-exceptions/cause-keyword)))
             true
             false))))
      (dosync (alter node-hierarchy/tree #(dissoc % ::TestNode)))
      (cljtest/is (and
                   (node-hierarchy/tree node-types/Node)
                   (= (count @node-hierarchy/tree) 1))))))


(cljtest/run-tests 'node-define)