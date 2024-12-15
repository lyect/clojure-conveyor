(ns node-create
  (:require [clojure.test                              :as cljtest]
            [blocks.channel.definitions.integer.fields :as integer-channel-fields]
            [blocks.channel.definitions.integer.def    :as integer-channel-def]
            [blocks.channel.methods                    :as channel-methods]
            [blocks.channel.types                      :as channel-types]
            [blocks.node.base                          :as node-base]
            [blocks.node.exceptions                    :as node-exceptions]
            [blocks.node.methods                       :as node-methods]
            [blocks.node.properties                    :as node-properties]
            [blocks.node.types                         :as node-types]
            [utils]))


(integer-channel-def/define-integer-channel)


(intern 'blocks.node.types    'types-list [node-types/Node ::TestNode ::DerivedTestNode ::UndefinedTestNode])


(defn- test-node-function
  [node x y]
  (channel-methods/create channel-types/IntegerT
                          integer-channel-fields/value (+ (channel-methods/get-channel-field x integer-channel-fields/value)
                                                          (channel-methods/get-channel-field y integer-channel-fields/value)
                                                          (node-methods/get-node-field node ::f1))))

(defn- derived-test-node-function
  [node x y z]
  (channel-methods/create channel-types/IntegerT
                          integer-channel-fields/value (+ (channel-methods/get-channel-field x integer-channel-fields/value)
                                                          (channel-methods/get-channel-field y integer-channel-fields/value)
                                                          (channel-methods/get-channel-field z integer-channel-fields/value)
                                                          (node-methods/get-node-field node ::f1)
                                                          (nth (node-methods/get-node-field node ::f4) 0))))

(node-base/define-node-type ::TestNode
                            node-properties/inputs   (list channel-types/IntegerT channel-types/IntegerT)
                            node-properties/outputs  (list channel-types/IntegerT)
                            node-properties/function test-node-function
                            node-properties/fields   '(::f1 ::f2 ::f3))
(node-base/define-node-type ::DerivedTestNode
                            node-properties/super-name ::TestNode
                            node-properties/inputs     (list channel-types/IntegerT channel-types/IntegerT channel-types/IntegerT)
                            node-properties/outputs    (list channel-types/IntegerT)
                            node-properties/function   derived-test-node-function
                            node-properties/fields     '(::f4))


(cljtest/deftest node-creation
  (cljtest/testing "Node creation test"
    (dosync
     (let [test-node (node-methods/create ::TestNode "TestNode" ::f1 8 ::f2 "str" ::f3 ::arg)]
       (cljtest/is (=                  (node-methods/get-node-type    test-node) ::TestNode))
       (cljtest/is (=                  (node-methods/get-node-super   test-node) node-types/Node))
       (cljtest/is (=                  (node-methods/get-node-name    test-node) "TestNode"))
       (cljtest/is (utils/lists-equal? (node-methods/get-node-inputs  test-node) (list channel-types/IntegerT channel-types/IntegerT)))
       (cljtest/is (utils/lists-equal? (node-methods/get-node-outputs test-node) (list channel-types/IntegerT)))
       (cljtest/is (utils/lists-equal? (node-methods/get-node-fields  test-node) '(::f1 ::f2 ::f3)))

       (cljtest/is (= (node-methods/get-node-field test-node ::f1) 8))
       (cljtest/is (= (node-methods/get-node-field test-node ::f2) "str"))
       (cljtest/is (= (node-methods/get-node-field test-node ::f3) ::arg))

       (let [test-channel1 (channel-methods/create channel-types/IntegerT integer-channel-fields/value 15)
             test-channel2 (channel-methods/create channel-types/IntegerT integer-channel-fields/value  2)]
         (cljtest/is (= (channel-methods/get-channel-field (nth (node-methods/execute test-node
                                                                                      test-channel1
                                                                                      test-channel2)
                                                                0)
                                                           integer-channel-fields/value) 25)))
       (let [test-channel1 (channel-methods/create channel-types/IntegerT integer-channel-fields/value -100)
             test-channel2 (channel-methods/create channel-types/IntegerT integer-channel-fields/value   30)]
         (cljtest/is (= (channel-methods/get-channel-field (nth (node-methods/execute test-node
                                                                                      test-channel1
                                                                                      test-channel2)
                                                                0)
                                                           integer-channel-fields/value) -62)))
       (let [test-channel1 (channel-methods/create channel-types/IntegerT integer-channel-fields/value 0)
             test-channel2 (channel-methods/create channel-types/IntegerT integer-channel-fields/value 0)]
         (cljtest/is (= (channel-methods/get-channel-field (nth (node-methods/execute test-node
                                                                                      test-channel1
                                                                                      test-channel2)
                                                                0)
                                                           integer-channel-fields/value) 8)))))))

(cljtest/deftest node-creation-derived
  (cljtest/testing "Derived node creation test"
    (dosync
     (let [derived-test-node (node-methods/create ::DerivedTestNode "DerivedTestNode" ::f1 8 ::f2 "str" ::f3 ::arg ::f4 '(1 2 3))]
       (cljtest/is (=                  (node-methods/get-node-type    derived-test-node) ::DerivedTestNode))
       (cljtest/is (=                  (node-methods/get-node-super   derived-test-node) ::TestNode))
       (cljtest/is (=                  (node-methods/get-node-name    derived-test-node) "DerivedTestNode"))
       (cljtest/is (utils/lists-equal? (node-methods/get-node-inputs  derived-test-node) (list channel-types/IntegerT channel-types/IntegerT channel-types/IntegerT)))
       (cljtest/is (utils/lists-equal? (node-methods/get-node-outputs derived-test-node) (list channel-types/IntegerT)))
       (cljtest/is (utils/lists-equal? (node-methods/get-node-fields  derived-test-node) '(::f1 ::f2 ::f3 ::f4)))

       (cljtest/is (=                  (node-methods/get-node-field derived-test-node ::f1) 8))
       (cljtest/is (=                  (node-methods/get-node-field derived-test-node ::f2) "str"))
       (cljtest/is (=                  (node-methods/get-node-field derived-test-node ::f3) ::arg))
       (cljtest/is (utils/lists-equal? (node-methods/get-node-field derived-test-node ::f4) '(1 2 3)))

       (let [test-channel1 (channel-methods/create channel-types/IntegerT integer-channel-fields/value 15)
             test-channel2 (channel-methods/create channel-types/IntegerT integer-channel-fields/value  2)
             test-channel3 (channel-methods/create channel-types/IntegerT integer-channel-fields/value 11)]
         (cljtest/is (= (channel-methods/get-channel-field (nth (node-methods/execute derived-test-node
                                                                                      test-channel1
                                                                                      test-channel2
                                                                                      test-channel3)
                                                                0)
                                                           integer-channel-fields/value) 37)))

       (let [test-channel1 (channel-methods/create channel-types/IntegerT integer-channel-fields/value -1)
             test-channel2 (channel-methods/create channel-types/IntegerT integer-channel-fields/value -5)
             test-channel3 (channel-methods/create channel-types/IntegerT integer-channel-fields/value -7)]
         (cljtest/is (= (channel-methods/get-channel-field (nth (node-methods/execute derived-test-node
                                                                                      test-channel1
                                                                                      test-channel2
                                                                                      test-channel3)
                                                                0)
                                                           integer-channel-fields/value) -4)))
       (let [test-channel1 (channel-methods/create channel-types/IntegerT integer-channel-fields/value 0)
             test-channel2 (channel-methods/create channel-types/IntegerT integer-channel-fields/value 0)
             test-channel3 (channel-methods/create channel-types/IntegerT integer-channel-fields/value 0)]
         (cljtest/is (= (channel-methods/get-channel-field (nth (node-methods/execute derived-test-node
                                                                                      test-channel1
                                                                                      test-channel2
                                                                                      test-channel3)
                                                                0)
                                                           integer-channel-fields/value) 9)))))))

(cljtest/deftest node-creation-type-undeclared
  (cljtest/testing "Node with undeclared type creation test"
    (cljtest/is
     (try
       (dosync (node-methods/create ::UndeclaredTestNode "UndeclaredTestNode"))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/create          (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/type-undeclared (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))))

(cljtest/deftest node-creation-type-undefined
  (cljtest/testing "Node with undefined type creation test"
    (cljtest/is
     (try
       (dosync (node-methods/create ::UndefinedTestNode "UndefinedTestNode"))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/create         (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/type-undefined (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))))

(cljtest/deftest node-creation-abstract
  (cljtest/testing "Abstract node creation test"
    (cljtest/is
     (try
       (dosync (node-methods/create node-types/Node "Node"))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/create            (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/abstract-creation (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))))

(cljtest/deftest node-creation-duplicating-fields
  (cljtest/testing "Node with duplicating fields creation test"
    (cljtest/is
     (try
       (dosync (node-methods/create ::TestNode "TestNode" ::f1 8 ::f2 "str" ::f1 1))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/create             (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/duplicating-fields (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))))

(cljtest/deftest node-creation-missing-fields
  (cljtest/testing "Node with missing fields creation test"
    (cljtest/is
     (try
       (dosync (node-methods/create ::TestNode "TestNode" ::f1 8 ::f2 "str"))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/create         (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/missing-fields (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))))

(cljtest/deftest node-creation-excess-fields
  (cljtest/testing "Node with excess fields creation test"
    (cljtest/is
     (try
       (dosync (node-methods/create ::TestNode "TestNode" ::f1 8 ::f2 "str" ::f3 ::arg ::f4 '(1 2 3)))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= node-exceptions/create        (-> e ex-data node-exceptions/type-keyword))
                  (= node-exceptions/excess-fields (-> e ex-data node-exceptions/cause-keyword)))
           true
           false))))))


(cljtest/run-tests 'node-create)
