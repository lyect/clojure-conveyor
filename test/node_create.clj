(ns node-create
  (:require [clojure.test                             :as cljtest]
            [blocks.channel.definitions.integer.def   :as integer-channel-def]
            [blocks.channel.definitions.number.fields :as number-channel-fields]
            [blocks.channel.methods                   :as channel-methods]
            [blocks.channel.types                     :as channel-types]
            [blocks.node.base                         :as node-base]
            [blocks.node.definitions.node.def         :as base-node-def]
            [blocks.node.definitions.node.fields      :as base-node-fields]
            [blocks.node.exceptions                   :as node-exceptions]
            [blocks.node.methods                      :as node-methods]
            [blocks.node.properties                   :as node-properties]
            [blocks.node.types                        :as node-types]
            [utils]))


(intern 'blocks.node.types 'types-list [node-types/NodeT ::TestNode ::DerivedTestNode ::UndefinedTestNode])


(defn- test-node-ready-validator
  [node-ref]
  (reduce
   (fn [res [input-buffer-amount input-buffer-ref]]
     (and res (<= input-buffer-amount (count @input-buffer-ref))))
   true
   (utils/zip (node-ref base-node-fields/input-buffers-amounts) (node-ref base-node-fields/input-buffers))))

(defn- test-node-function
  [node-ref]
  (let [input-buffer-ref1 (nth (node-ref base-node-fields/input-buffers)  0)
        input-buffer-ref2 (nth (node-ref base-node-fields/input-buffers)  1)
        output-buffer-ref (nth (node-ref base-node-fields/output-buffers) 0)
        number1           (first @input-buffer-ref1)
        number2           (first @input-buffer-ref2)]
    (alter input-buffer-ref1 #(rest %))
    (alter input-buffer-ref2 #(rest %))
    (alter output-buffer-ref #(conj %
                                    (channel-methods/create channel-types/IntegerT
                                                            number-channel-fields/value (+ (channel-methods/get-channel-field number1 number-channel-fields/value)
                                                                                           (channel-methods/get-channel-field number2 number-channel-fields/value)
                                                                                           (node-methods/get-node-field node-ref ::f1)))))))

(defn- derived-test-node-function
  [node-ref]
  (let [input-buffer-ref1 (nth (node-ref base-node-fields/input-buffers)  0)
        input-buffer-ref2 (nth (node-ref base-node-fields/input-buffers)  1)
        input-buffer-ref3 (nth (node-ref base-node-fields/input-buffers)  2)
        output-buffer-ref (nth (node-ref base-node-fields/output-buffers) 0)
        number1           (first @input-buffer-ref1)
        number2           (first @input-buffer-ref2)
        number3           (first @input-buffer-ref3)]
    (alter input-buffer-ref1 #(rest %))
    (alter input-buffer-ref2 #(rest %))
    (alter input-buffer-ref3 #(rest %))
    (alter output-buffer-ref #(conj %
                                    (channel-methods/create channel-types/IntegerT
                                                            number-channel-fields/value (+ (channel-methods/get-channel-field number1 number-channel-fields/value)
                                                                                           (channel-methods/get-channel-field number2 number-channel-fields/value)
                                                                                           (channel-methods/get-channel-field number3 number-channel-fields/value)
                                                                                           (node-methods/get-node-field node-ref ::f1)
                                                                                           (nth (node-methods/get-node-field node-ref ::f4) 0)))))))

(integer-channel-def/define-integer-channel)

(base-node-def/define-base-node)

(node-base/define-node-type ::TestNode
                            node-properties/inputs          (list channel-types/NumberT channel-types/NumberT)
                            node-properties/outputs         (list channel-types/NumberT)
                            node-properties/ready-validator test-node-ready-validator
                            node-properties/function        test-node-function
                            node-properties/fields          '(::f1 ::f2 ::f3))

(node-base/define-node-type ::DerivedTestNode
                            node-properties/super-name ::TestNode
                            node-properties/inputs     (list channel-types/NumberT channel-types/NumberT channel-types/NumberT)
                            node-properties/outputs    (list channel-types/NumberT)
                            node-properties/function   derived-test-node-function
                            node-properties/fields     '(::f4))


(cljtest/deftest node-creation
  (cljtest/testing "Node creation test"
    (let [test-node (dosync (node-methods/create ::TestNode "TestNode" ::f1 8 ::f2 "str" ::f3 ::arg))]
      (cljtest/is (=                  (node-methods/get-node-type-name  test-node) ::TestNode))
      (cljtest/is (=                  (node-methods/get-node-super-name test-node) node-types/NodeT))
      (cljtest/is (=                  (node-methods/get-node-name       test-node) "TestNode"))
      (cljtest/is (utils/lists-equal? (node-methods/get-node-inputs     test-node) (list channel-types/NumberT channel-types/NumberT)))
      (cljtest/is (utils/lists-equal? (node-methods/get-node-outputs    test-node) (list channel-types/NumberT)))
      (cljtest/is (utils/lists-equal? (node-methods/get-node-fields     test-node) (list ::f1 ::f2 ::f3)))

      (cljtest/is (= (node-methods/get-node-field test-node ::f1) 8))
      (cljtest/is (= (node-methods/get-node-field test-node ::f2) "str"))
      (cljtest/is (= (node-methods/get-node-field test-node ::f3) ::arg))

      (dosync
       (node-methods/store test-node 0 (channel-methods/create channel-types/IntegerT number-channel-fields/value   15))
       (node-methods/store test-node 0 (channel-methods/create channel-types/IntegerT number-channel-fields/value -100))
       (node-methods/store test-node 0 (channel-methods/create channel-types/IntegerT number-channel-fields/value    0))

       (node-methods/store test-node 1 (channel-methods/create channel-types/IntegerT number-channel-fields/value    2))
       (node-methods/store test-node 1 (channel-methods/create channel-types/IntegerT number-channel-fields/value   30))
       (node-methods/store test-node 1 (channel-methods/create channel-types/IntegerT number-channel-fields/value    0))

       (let [input-buffer1 @(nth (test-node base-node-fields/input-buffers) 0)
             input-buffer2 @(nth (test-node base-node-fields/input-buffers) 1)]
         (cljtest/is (= (count input-buffer1) 3))
         (cljtest/is (= (count input-buffer2) 3))
         (cljtest/is (= (channel-methods/get-channel-type-name (nth input-buffer1 0)) channel-types/IntegerT))
         (cljtest/is (= (channel-methods/get-channel-type-name (nth input-buffer1 1)) channel-types/IntegerT))
         (cljtest/is (= (channel-methods/get-channel-type-name (nth input-buffer1 2)) channel-types/IntegerT))
         (cljtest/is (= (channel-methods/get-channel-field (nth input-buffer1 0) number-channel-fields/value)   15))
         (cljtest/is (= (channel-methods/get-channel-field (nth input-buffer1 1) number-channel-fields/value) -100))
         (cljtest/is (= (channel-methods/get-channel-field (nth input-buffer1 2) number-channel-fields/value)    0))
         (cljtest/is (= (channel-methods/get-channel-type-name (nth input-buffer2 0)) channel-types/IntegerT))
         (cljtest/is (= (channel-methods/get-channel-type-name (nth input-buffer2 1)) channel-types/IntegerT))
         (cljtest/is (= (channel-methods/get-channel-type-name (nth input-buffer2 2)) channel-types/IntegerT))
         (cljtest/is (= (channel-methods/get-channel-field (nth input-buffer2 0) number-channel-fields/value)    2))
         (cljtest/is (= (channel-methods/get-channel-field (nth input-buffer2 1) number-channel-fields/value)   30))
         (cljtest/is (= (channel-methods/get-channel-field (nth input-buffer2 2) number-channel-fields/value)    0)))

       (node-methods/execute test-node)

       (let [output (node-methods/flush-output test-node 0)]
         (cljtest/is (= (count output) 3))
         (let [out1 (nth output 0)
               out2 (nth output 1)
               out3 (nth output 2)]
           (cljtest/is (= (channel-methods/get-channel-type-name out1) channel-types/IntegerT))
           (cljtest/is (= (channel-methods/get-channel-type-name out2) channel-types/IntegerT))
           (cljtest/is (= (channel-methods/get-channel-type-name out3) channel-types/IntegerT))
           (cljtest/is (= (channel-methods/get-channel-field out1 number-channel-fields/value)  25))
           (cljtest/is (= (channel-methods/get-channel-field out2 number-channel-fields/value) -62))
           (cljtest/is (= (channel-methods/get-channel-field out3 number-channel-fields/value)   8))))))))

(cljtest/deftest node-creation-derived
  (cljtest/testing "Derived node creation test"
     (let [derived-test-node (dosync (node-methods/create ::DerivedTestNode "DerivedTestNode" ::f1 8 ::f2 "str" ::f3 ::arg ::f4 '(1 2 3)))]
       (cljtest/is (=                  (node-methods/get-node-type-name  derived-test-node) ::DerivedTestNode))
       (cljtest/is (=                  (node-methods/get-node-super-name derived-test-node) ::TestNode))
       (cljtest/is (=                  (node-methods/get-node-name       derived-test-node) "DerivedTestNode"))
       (cljtest/is (utils/lists-equal? (node-methods/get-node-inputs     derived-test-node) (list channel-types/NumberT channel-types/NumberT channel-types/NumberT)))
       (cljtest/is (utils/lists-equal? (node-methods/get-node-outputs    derived-test-node) (list channel-types/NumberT)))
       (cljtest/is (utils/lists-equal? (node-methods/get-node-fields     derived-test-node) (list ::f1 ::f2 ::f3 ::f4)))

       (cljtest/is (=                  (node-methods/get-node-field derived-test-node ::f1) 8))
       (cljtest/is (=                  (node-methods/get-node-field derived-test-node ::f2) "str"))
       (cljtest/is (=                  (node-methods/get-node-field derived-test-node ::f3) ::arg))
       (cljtest/is (utils/lists-equal? (node-methods/get-node-field derived-test-node ::f4) '(1 2 3)))
       
       (dosync
        (node-methods/store derived-test-node 0 (channel-methods/create channel-types/IntegerT number-channel-fields/value 15))
        (node-methods/store derived-test-node 0 (channel-methods/create channel-types/IntegerT number-channel-fields/value -1))
        (node-methods/store derived-test-node 0 (channel-methods/create channel-types/IntegerT number-channel-fields/value  0))

        (node-methods/store derived-test-node 1 (channel-methods/create channel-types/IntegerT number-channel-fields/value  2))
        (node-methods/store derived-test-node 1 (channel-methods/create channel-types/IntegerT number-channel-fields/value -5))
        (node-methods/store derived-test-node 1 (channel-methods/create channel-types/IntegerT number-channel-fields/value  0))

        (node-methods/store derived-test-node 2 (channel-methods/create channel-types/IntegerT number-channel-fields/value 11))
        (node-methods/store derived-test-node 2 (channel-methods/create channel-types/IntegerT number-channel-fields/value -7))
        (node-methods/store derived-test-node 2 (channel-methods/create channel-types/IntegerT number-channel-fields/value  0))

        (let [input-buffer1 @(nth (derived-test-node base-node-fields/input-buffers) 0)
              input-buffer2 @(nth (derived-test-node base-node-fields/input-buffers) 1)
              input-buffer3 @(nth (derived-test-node base-node-fields/input-buffers) 2)]
          (cljtest/is (= (count input-buffer1) 3))
          (cljtest/is (= (count input-buffer2) 3))
          (cljtest/is (= (channel-methods/get-channel-type-name (nth input-buffer1 0)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-channel-type-name (nth input-buffer1 1)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-channel-type-name (nth input-buffer1 2)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-channel-field (nth input-buffer1 0) number-channel-fields/value) 15))
          (cljtest/is (= (channel-methods/get-channel-field (nth input-buffer1 1) number-channel-fields/value) -1))
          (cljtest/is (= (channel-methods/get-channel-field (nth input-buffer1 2) number-channel-fields/value)  0))
          (cljtest/is (= (channel-methods/get-channel-type-name (nth input-buffer2 0)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-channel-type-name (nth input-buffer2 1)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-channel-type-name (nth input-buffer2 2)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-channel-field (nth input-buffer2 0) number-channel-fields/value)  2))
          (cljtest/is (= (channel-methods/get-channel-field (nth input-buffer2 1) number-channel-fields/value) -5))
          (cljtest/is (= (channel-methods/get-channel-field (nth input-buffer2 2) number-channel-fields/value)  0))
          (cljtest/is (= (channel-methods/get-channel-type-name (nth input-buffer3 0)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-channel-type-name (nth input-buffer3 1)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-channel-type-name (nth input-buffer3 2)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-channel-field (nth input-buffer3 0) number-channel-fields/value) 11))
          (cljtest/is (= (channel-methods/get-channel-field (nth input-buffer3 1) number-channel-fields/value) -7))
          (cljtest/is (= (channel-methods/get-channel-field (nth input-buffer3 2) number-channel-fields/value)  0)))

        (node-methods/execute derived-test-node)

        (let [output (node-methods/flush-output derived-test-node 0)]
          (cljtest/is (= (count output) 3))
          (let [out1 (nth output 0)
                out2 (nth output 1)
                out3 (nth output 2)]
            (cljtest/is (= (channel-methods/get-channel-type-name out1) channel-types/IntegerT))
            (cljtest/is (= (channel-methods/get-channel-type-name out2) channel-types/IntegerT))
            (cljtest/is (= (channel-methods/get-channel-type-name out3) channel-types/IntegerT))
            (cljtest/is (= (channel-methods/get-channel-field out1 number-channel-fields/value) 37))
            (cljtest/is (= (channel-methods/get-channel-field out2 number-channel-fields/value) -4))
            (cljtest/is (= (channel-methods/get-channel-field out3 number-channel-fields/value)  9))))))))

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
       (dosync (node-methods/create node-types/NodeT "Node"))
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
