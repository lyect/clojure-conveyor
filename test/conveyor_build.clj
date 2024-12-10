(ns conveyor-build
  (:require [clojure.test :as cljtest]
            [conveyors.channel.base :as channel-base]
            [conveyors.channel.methods :as channel-methods]
            [conveyors.channel.properties :as channel-properties]
            [conveyors.conveyor.base :as conveyor-base]
            [conveyors.conveyor.exceptions :as conveyor-exceptions]
            [conveyors.conveyor.methods :as conveyor-methods]
            [conveyors.node.base :as node-base]
            [conveyors.node.methods :as node-methods]
            [conveyors.node.properties :as node-properties])
  (:import (clojure.lang ExceptionInfo)))


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

(def test-node1 (node-methods/create ::TestNode ::f1 8 ::f2 "str" ::f3 ::arg))
(def test-node2 (node-methods/create ::DerivedTestNode ::f1 8 ::f2 "str" ::f3 ::arg ::f4 '(1 2 3)))
(def test-node3 (node-methods/create ::DerivedTestNode ::f1 0 ::f2 0 ::f3 0 ::f4 '()))


(cljtest/deftest conveyor-building
  (cljtest/testing "Conveyor building test"
    (let [edges (list (list test-node2 1 test-node1 1)
                      (list test-node2 2 test-node1 2)
                      (list test-node1 1 test-node3 2))
          conv (apply conveyor-base/build-conveyor edges)]
      (cljtest/is (conveyor-methods/producer? conv test-node1))
      (cljtest/is (conveyor-methods/producer? conv test-node2))
      (cljtest/is (not (conveyor-methods/producer? conv test-node3)))
      (cljtest/is (conveyor-methods/consumer? conv test-node1))
      (cljtest/is (conveyor-methods/consumer? conv test-node3))
      (cljtest/is (not (conveyor-methods/consumer? conv test-node2))))))


(defn right-build-exception? [e right-e]
  (and (= conveyor-exceptions/build (-> e ex-data conveyor-exceptions/type-keyword))
       (= right-e (-> e ex-data conveyor-exceptions/cause-keyword))))

(cljtest/deftest conveyor-building-incorrect-edge
  (cljtest/testing "Incorrect edge for conveyor building test"
    (cljtest/is (try (conveyor-base/build-conveyor (list test-node2 2 test-node1))
                     (catch ExceptionInfo e (right-build-exception? e conveyor-exceptions/incorrect-edge))))
    (cljtest/is (try (conveyor-base/build-conveyor (list test-node1 1 test-node2 2 5))
                     (catch ExceptionInfo e (right-build-exception? e conveyor-exceptions/incorrect-edge))))))

(cljtest/deftest conveyor-building-incompatible-channels
  (cljtest/testing "Conveyor with incompatible channels building test"
    (cljtest/is (try (conveyor-base/build-conveyor (list test-node2 1 test-node1 2))
                     (catch ExceptionInfo e (right-build-exception? e conveyor-exceptions/incompatible-channels))))))

(cljtest/deftest conveyor-building-non-existent-channel
  (cljtest/testing "Conveyor with non-existent channels building test"
    (cljtest/is (try (conveyor-base/build-conveyor (list test-node1 2 test-node2 1))
                     (catch ExceptionInfo e (right-build-exception? e conveyor-exceptions/non-existent-channel))))
    (cljtest/is (try (conveyor-base/build-conveyor (list test-node1 1 test-node2 3))
                     (catch ExceptionInfo e (right-build-exception? e conveyor-exceptions/non-existent-channel))))
    (cljtest/is (try (conveyor-base/build-conveyor (list test-node1 2 test-node2 3))
                     (catch ExceptionInfo e (right-build-exception? e conveyor-exceptions/non-existent-channel))))))

(cljtest/deftest conveyor-building-twice-use
  (cljtest/testing "Conveyor with twice used channels building test"
    (cljtest/is (try (conveyor-base/build-conveyor (list test-node2 1 test-node1 1)
                                                   (list test-node2 2 test-node1 2)
                                                   (list test-node1 1 test-node3 2)
                                                   (list test-node1 1 test-node3 1))
                     (catch ExceptionInfo e (right-build-exception? e conveyor-exceptions/twice-use))))
    (cljtest/is (try (conveyor-base/build-conveyor (list test-node2 1 test-node1 1)
                                                   (list test-node2 2 test-node1 2)
                                                   (list test-node1 1 test-node3 2)
                                                   (list test-node3 1 test-node1 1))
                     (catch ExceptionInfo e (right-build-exception? e conveyor-exceptions/twice-use))))))

(cljtest/run-tests 'conveyor-build)
