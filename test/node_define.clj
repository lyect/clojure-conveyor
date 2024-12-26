(ns node-define
  (:require [clojure.test                           :as cljtest]
            [blocks.channel.types                   :as channel-types]
            [blocks.node.base                       :as node-base]
            [blocks.node.definitions.node.def       :as base-node-def]
            [blocks.node.definitions.node.fields    :as base-node-fields]
            [blocks.node.exceptions                 :as node-exceptions]
            [blocks.node.hierarchy                  :as node-hierarchy]
            [blocks.node.properties                 :as node-properties]
            [blocks.node.types                      :as node-types]
            [utils]))


(intern 'blocks.channel.types 'types-list [channel-types/ChannelT ::TestChannel1 ::TestChannel2 ::TestChannel3])

(intern 'blocks.node.types 'types-list [node-types/NodeT ::TestNode ::DerivedTestNode])


(base-node-def/define-base-node)


(cljtest/deftest node-define
  (cljtest/testing "Node definition test"
    (node-base/define-node-type ::TestNode
                                node-properties/inputs   (list ::TestChannel1 ::TestChannel2)
                                node-properties/outputs  (list ::TestChannel3)
                                node-properties/function (fn [x] (inc x))
                                node-properties/fields   '(::f1 ::f2 ::f3))
    (cljtest/is (node-types/defined? ::TestNode))
    (let [test-node-type (node-hierarchy/tree ::TestNode)]
      (cljtest/is (=                  (test-node-type node-properties/type-name)  ::TestNode))
      (cljtest/is (=                  (test-node-type node-properties/super-name) node-types/NodeT))
      (cljtest/is (utils/lists-equal? (test-node-type node-properties/inputs)     (list ::TestChannel1 ::TestChannel2)))
      (cljtest/is (utils/lists-equal? (test-node-type node-properties/outputs)    (list ::TestChannel3)))
      (cljtest/is (utils/lists-equal? (test-node-type node-properties/fields)     (concat (list ::f1 ::f2 ::f3) base-node-fields/fields-list))))
    (dosync (alter node-hierarchy/tree #(dissoc % ::TestNode)))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))

(cljtest/deftest node-define-derived
  (cljtest/testing "Derived node definition test"
    (node-base/define-node-type ::TestNode
                                node-properties/inputs   (list ::TestChannel1 ::TestChannel2)
                                node-properties/outputs  (list ::TestChannel3)
                                node-properties/function (fn [x] (inc x))
                                node-properties/fields   '(::f1 ::f2 ::f3))
    (node-base/define-node-type ::DerivedTestNode
                                node-properties/super-name ::TestNode
                                node-properties/inputs   (list ::TestChannel3 ::TestChannel3)
                                node-properties/function (fn [x] (+ x 10))
                                node-properties/fields   '(::f4))
    (cljtest/is (node-types/defined? ::DerivedTestNode))
    (let [derived-test-node-type (node-hierarchy/tree ::DerivedTestNode)]
      (cljtest/is (=                  (derived-test-node-type node-properties/type-name)  ::DerivedTestNode))
      (cljtest/is (=                  (derived-test-node-type node-properties/super-name) ::TestNode))
      (cljtest/is (utils/lists-equal? (derived-test-node-type node-properties/inputs)     (list ::TestChannel3 ::TestChannel3)))
      (cljtest/is (utils/lists-equal? (derived-test-node-type node-properties/outputs)    (list ::TestChannel3)))
      (cljtest/is (utils/lists-equal? (derived-test-node-type node-properties/fields)     (concat (list ::f1 ::f2 ::f3 ::f4) base-node-fields/fields-list))))
    (dosync (alter node-hierarchy/tree #(dissoc % ::TestNode)))
    (dosync (alter node-hierarchy/tree #(dissoc % ::DerivedTestNode)))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))

(cljtest/deftest node-define-type-undeclared
  (cljtest/testing "Node with undeclared type definition test"
    (cljtest/is
     (try
       (node-base/define-node-type ::UndeclaredTestNode)
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/define-node-type (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/type-undeclared  (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))

(cljtest/deftest node-define-super-undeclared
  (cljtest/testing "Node with undeclared super definition test"
    (cljtest/is
     (try
       (node-base/define-node-type ::TestNode
                                   node-properties/super-name ::UndeclaredTestNode)
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/define-node-type (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/super-undeclared (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))

(cljtest/deftest node-define-super-undefined
  (cljtest/testing "Node with undefined super definition test"
    (cljtest/is
     (try
       (node-base/define-node-type ::DerivedTestNode
                                   node-properties/super-name ::TestNode)
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/define-node-type (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/super-undefined  (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))

(cljtest/deftest node-define-duplicated-fields
  (cljtest/testing "Node with duplicated fields definition test"
    (cljtest/is
     (try
       (node-base/define-node-type ::TestNode
                                   node-properties/inputs   (list ::TestChannel1 ::TestChannel2)
                                   node-properties/outputs  (list ::TestChannel3)
                                   node-properties/function (fn [x] (inc x))
                                   node-properties/fields   '(::f1 ::f2 ::f3 ::f2))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/define-node-type   (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/duplicating-fields (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))

(cljtest/deftest node-define-super-intersected-fields
  (cljtest/testing "Derived node with fields intersecting super's fields definition test"
    (node-base/define-node-type ::TestNode
                                node-properties/inputs   (list ::TestChannel1 ::TestChannel2)
                                node-properties/outputs  (list ::TestChannel3)
                                node-properties/function (fn [x] (inc x))
                                node-properties/fields   '(::f1 ::f2 ::f3))
    (cljtest/is
     (try
       (node-base/define-node-type ::DerivedTestNode
                                   node-properties/super-name ::TestNode
                                   node-properties/fields     '(::f1))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/define-node-type          (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/super-fields-intersection (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))
    (dosync (alter node-hierarchy/tree #(dissoc % ::TestNode)))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))

(cljtest/deftest node-define-inputs-undefined
  (cljtest/testing "Node with undefined inputs definition test"
    (cljtest/is
     (try
       (node-base/define-node-type ::TestNode
                                   node-properties/outputs  (list ::TestChannel3)
                                   node-properties/function (fn [x] (inc x))
                                   node-properties/fields   '(::f1 ::f2 ::f3))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/define-node-type (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/inputs-undefined (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))

(cljtest/deftest node-define-outputs-undefined
  (cljtest/testing "Node with undefined outputs definition test"
    (cljtest/is
     (try
       (node-base/define-node-type ::TestNode
                                   node-properties/inputs   (list ::TestChannel1 ::TestChannel2)
                                   node-properties/function (fn [x] (inc x))
                                   node-properties/fields   '(::f1 ::f2 ::f3))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/define-node-type  (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/outputs-undefined (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))

(cljtest/deftest node-define-function-undefined
  (cljtest/testing "Node with undefined function definition test"
    (cljtest/is
     (try
       (node-base/define-node-type ::TestNode
                                   node-properties/inputs  (list ::TestChannel1 ::TestChannel2)
                                   node-properties/outputs (list ::TestChannel3)
                                   node-properties/fields  '(::f1 ::f2 ::f3))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/define-node-type   (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/function-undefined (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))

(cljtest/deftest node-redefine
  (cljtest/testing "Node redefinition test"
    (node-base/define-node-type ::TestNode
                                node-properties/inputs   (list ::TestChannel1 ::TestChannel2)
                                node-properties/outputs  (list ::TestChannel3)
                                node-properties/function (fn [x] (inc x))
                                node-properties/fields   '(::f1 ::f2 ::f3))
    (cljtest/is
     (try
       (node-base/define-node-type ::TestNode
                                   node-properties/inputs   (list ::TestChannel2 ::TestChannel3)
                                   node-properties/outputs  (list ::TestChannel1 ::TestChannel1)
                                   node-properties/function (fn [x] (+ 3 x))
                                   node-properties/fields   '(::t1 ::t2))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/define-node-type (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/type-defined     (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))
    (dosync (alter node-hierarchy/tree #(dissoc % ::TestNode)))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))

(cljtest/deftest node-define-unvalidated-inputs
  (cljtest/testing "Node with unvalidated inputs definition test"
    (cljtest/is
     (try
       (node-base/define-node-type ::TestNode
                                   node-properties/inputs   (list ::UndeclaredChannel)
                                   node-properties/outputs  (list ::TestChannel3)
                                   node-properties/function (fn [x] (inc x))
                                   node-properties/fields   '(::f1 ::f2 ::f3))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/define-node-type   (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/inputs-unvalidated (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))

(cljtest/deftest node-define-unvalidated-outputs
  (cljtest/testing "Node with unvalidated outputs definition test"
    (cljtest/is
     (try
       (node-base/define-node-type ::TestNode
                                   node-properties/inputs   (list ::TestChannel1)
                                   node-properties/outputs  (list ::UndeclaredChannel)
                                   node-properties/function (fn [x] (inc x))
                                   node-properties/fields   '(::f1 ::f2 ::f3))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/define-node-type    (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/outputs-unvalidated (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))

(cljtest/deftest node-define-unvalidated-ready-validator
  (cljtest/testing "Node with unvalidated ready validator definition test"
    (cljtest/is
     (try
       (node-base/define-node-type ::TestNode
                                   node-properties/inputs          (list ::TestChannel1)
                                   node-properties/outputs         (list ::TestChannel2)
                                   node-properties/ready-validator '(1)
                                   node-properties/function        (fn [x] (nil? x))
                                   node-properties/fields          '(::f1 ::f2 ::f3))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/define-node-type            (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/ready-validator-unvalidated (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))

(cljtest/deftest node-define-unvalidated-function
  (cljtest/testing "Node with unvalidated function definition test"
    (cljtest/is
     (try
       (node-base/define-node-type ::TestNode
                                   node-properties/inputs   (list ::TestChannel1)
                                   node-properties/outputs  (list ::TestChannel2)
                                   node-properties/function '(1)
                                   node-properties/fields   '(::f1 ::f2 ::f3))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/define-node-type     (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/function-unvalidated (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))


(cljtest/run-tests 'node-define)
