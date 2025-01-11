(ns node-create
  (:require
   [blocks.channel.definitions.integer.def   :as integer-channel-def]
   [blocks.channel.definitions.number.fields :as number-channel-fields]
   [blocks.channel.methods                   :as channel-methods]
   [blocks.channel.types                     :as channel-types]
   [blocks.node.definitions.node.def         :as base-node-def]
   [blocks.node.definitions.node.fields      :as base-node-fields]
   [blocks.node.input.methods                :as node-input-methods]
   [blocks.node.link.methods                 :as node-link-methods]
   [blocks.node.methods                      :as node-methods]
   [blocks.node.output.methods               :as node-output-methods]
   [blocks.node.properties                   :as node-properties]
   [blocks.node.types                        :as node-types]
   [clojure.test                             :as cljtest]
   [utils]))


(intern 'blocks.node.types 'types-tags-list [node-types/NodeT ::TestNode ::DerivedTestNode])
(intern 'blocks.node.types 'abstract-types-tags-list [])


(defn- handler1
  [node-fields inputs [number3]]
  (let [number1           (nth inputs 0)
        number2           (nth inputs 1)]
    {1 [(channel-methods/create channel-types/IntegerT
                              number-channel-fields/value (+ (channel-methods/get-field-value number1 number-channel-fields/value)
                                                             (channel-methods/get-field-value number2 number-channel-fields/value)
                                                             (channel-methods/get-field-value number3 number-channel-fields/value)
                                                             (node-fields ::f1)))]}))

(defn- handler2
  [node-fields inputs]
  (let [number1           (nth inputs 0)
        number2           (nth inputs 1)
        number3           (nth inputs 2)
        number4           (nth inputs 3)]
    {1 [(channel-methods/create channel-types/IntegerT
                                number-channel-fields/value (* (channel-methods/get-field-value number1 number-channel-fields/value)
                                                               (channel-methods/get-field-value number2 number-channel-fields/value)
                                                               (node-fields ::f1)))]
     2 [(channel-methods/create channel-types/IntegerT
                                number-channel-fields/value (+ (* (channel-methods/get-field-value number3 number-channel-fields/value)
                                                                  (channel-methods/get-field-value number4 number-channel-fields/value)
                                                                  (nth (node-fields ::f4) 0))
                                                               (nth (node-fields ::f4) 1)
                                                               (nth (node-fields ::f4) 2)))]}))

(integer-channel-def/define)

(base-node-def/define)

(node-types/define "TestNode" ::TestNode
                   node-properties/inputs      [(node-input-methods/create 1 channel-types/IntegerT)
                                                (node-input-methods/create 2 channel-types/IntegerT)]
                   node-properties/outputs     [(node-output-methods/create 1 channel-types/IntegerT)]
                   node-properties/links       [(node-link-methods/create [1 2] [1] handler1)]
                   node-properties/fields-tags [::f1 ::f2 ::f3])

(node-types/define "DerivedTestNode" ::DerivedTestNode
                   node-properties/super-type-tag ::TestNode
                   node-properties/inputs         [(node-input-methods/create 3 channel-types/IntegerT)]
                   node-properties/outputs        [(node-output-methods/create 2 channel-types/IntegerT)]
                   node-properties/links          [(node-link-methods/create [3] [1] handler2)]
                   node-properties/fields-tags    [::f4])


(cljtest/deftest node-creation
  (cljtest/testing "Node creation test"
    (let [test-node (dosync (node-methods/create "TestNode" ::TestNode ::f1 8 ::f2 "str" ::f3 ::arg))]
      (dosync (node-methods/set-required-input-load test-node 1 2))

      (cljtest/is (=                  (node-methods/get-type-tag  test-node) ::TestNode))
      (cljtest/is (=                  (node-methods/get-label     test-node) "TestNode"))
      (cljtest/is (utils/lists-equal? (keys (node-methods/get-fields test-node)) [::f1 ::f2 ::f3]))

      (cljtest/is (= (node-methods/get-field-value test-node ::f1) 8))
      (cljtest/is (= (node-methods/get-field-value test-node ::f2) "str"))
      (cljtest/is (= (node-methods/get-field-value test-node ::f3) ::arg))

      (dosync
       (node-methods/store test-node 1 (channel-methods/create channel-types/IntegerT number-channel-fields/value 1))
       (node-methods/store test-node 1 (channel-methods/create channel-types/IntegerT number-channel-fields/value 2))
       (node-methods/store test-node 1 (channel-methods/create channel-types/IntegerT number-channel-fields/value 3))
       (node-methods/store test-node 1 (channel-methods/create channel-types/IntegerT number-channel-fields/value 4))

       (node-methods/store test-node 2 (channel-methods/create channel-types/IntegerT number-channel-fields/value 5))
       (node-methods/store test-node 2 (channel-methods/create channel-types/IntegerT number-channel-fields/value 6))

       (let [input-buffer1 @((test-node base-node-fields/inputs-buffers) 1)
             input-buffer2 @((test-node base-node-fields/inputs-buffers) 2)]
         (cljtest/is (= (count input-buffer1) 4))
         (cljtest/is (= (count input-buffer2) 2))
         (cljtest/is (= (channel-methods/get-type-tag (nth input-buffer1 0)) channel-types/IntegerT))
         (cljtest/is (= (channel-methods/get-type-tag (nth input-buffer1 1)) channel-types/IntegerT))
         (cljtest/is (= (channel-methods/get-type-tag (nth input-buffer1 2)) channel-types/IntegerT))
         (cljtest/is (= (channel-methods/get-type-tag (nth input-buffer1 3)) channel-types/IntegerT))
         (cljtest/is (= (channel-methods/get-field-value (nth input-buffer1 0) number-channel-fields/value) 1))
         (cljtest/is (= (channel-methods/get-field-value (nth input-buffer1 1) number-channel-fields/value) 2))
         (cljtest/is (= (channel-methods/get-field-value (nth input-buffer1 2) number-channel-fields/value) 3))
         (cljtest/is (= (channel-methods/get-field-value (nth input-buffer1 3) number-channel-fields/value) 4))
         (cljtest/is (= (channel-methods/get-type-tag (nth input-buffer2 0)) channel-types/IntegerT))
         (cljtest/is (= (channel-methods/get-type-tag (nth input-buffer2 1)) channel-types/IntegerT))
         (cljtest/is (= (channel-methods/get-field-value (nth input-buffer2 0) number-channel-fields/value) 5))
         (cljtest/is (= (channel-methods/get-field-value (nth input-buffer2 1) number-channel-fields/value) 6)))

       (node-methods/execute test-node)

       (let [output (node-methods/flush-output test-node 1)]
         (cljtest/is (= (count output) 2))
         (let [out1 (nth output 0)
               out2 (nth output 1)]
           (cljtest/is (= (channel-methods/get-type-tag out1) channel-types/IntegerT))
           (cljtest/is (= (channel-methods/get-type-tag out2) channel-types/IntegerT))
           (cljtest/is (= (channel-methods/get-field-value out1 number-channel-fields/value) 16))
           (cljtest/is (= (channel-methods/get-field-value out2 number-channel-fields/value) 21))))))))

(cljtest/deftest node-creation-derived
  (cljtest/testing "Derived node creation test"
     (let [derived-test-node (dosync (node-methods/create "DerivedTestNode" ::DerivedTestNode ::f1 8 ::f2 "str" ::f3 ::arg ::f4 [2 -1 3]))]
       (dosync (node-methods/set-required-input-load derived-test-node 1 2))
       (dosync (node-methods/set-required-input-load derived-test-node 3 4))

       (cljtest/is (=                  (node-methods/get-type-tag     derived-test-node)  ::DerivedTestNode))
       (cljtest/is (=                  (node-methods/get-label        derived-test-node)  "DerivedTestNode"))
       (cljtest/is (utils/lists-equal? (keys (node-methods/get-fields derived-test-node)) [::f1 ::f2 ::f3 ::f4]))

       (cljtest/is (=                  (node-methods/get-field-value derived-test-node ::f1) 8))
       (cljtest/is (=                  (node-methods/get-field-value derived-test-node ::f2) "str"))
       (cljtest/is (=                  (node-methods/get-field-value derived-test-node ::f3) ::arg))
       (cljtest/is (utils/lists-equal? (node-methods/get-field-value derived-test-node ::f4) [2 -1 3]))
       
       (dosync
        (node-methods/store derived-test-node 1 (channel-methods/create channel-types/IntegerT number-channel-fields/value 2))
        (node-methods/store derived-test-node 1 (channel-methods/create channel-types/IntegerT number-channel-fields/value 3))
        (node-methods/store derived-test-node 1 (channel-methods/create channel-types/IntegerT number-channel-fields/value 4))
        (node-methods/store derived-test-node 1 (channel-methods/create channel-types/IntegerT number-channel-fields/value 5))

        (node-methods/store derived-test-node 2 (channel-methods/create channel-types/IntegerT number-channel-fields/value 6))
        (node-methods/store derived-test-node 2 (channel-methods/create channel-types/IntegerT number-channel-fields/value 7))

        (let [input-buffer1 @((derived-test-node base-node-fields/inputs-buffers) 1)
              input-buffer2 @((derived-test-node base-node-fields/inputs-buffers) 2)]
          (cljtest/is (= (count input-buffer1) 4))
          (cljtest/is (= (count input-buffer2) 2))
          (cljtest/is (= (channel-methods/get-type-tag (nth input-buffer1 0)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-type-tag (nth input-buffer1 1)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-type-tag (nth input-buffer1 2)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-type-tag (nth input-buffer1 3)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-field-value (nth input-buffer1 0) number-channel-fields/value) 2))
          (cljtest/is (= (channel-methods/get-field-value (nth input-buffer1 1) number-channel-fields/value) 3))
          (cljtest/is (= (channel-methods/get-field-value (nth input-buffer1 2) number-channel-fields/value) 4))
          (cljtest/is (= (channel-methods/get-field-value (nth input-buffer1 3) number-channel-fields/value) 5))
          (cljtest/is (= (channel-methods/get-type-tag (nth input-buffer2 0)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-type-tag (nth input-buffer2 1)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-field-value (nth input-buffer2 0) number-channel-fields/value) 6))
          (cljtest/is (= (channel-methods/get-field-value (nth input-buffer2 1) number-channel-fields/value) 7)))

        (node-methods/execute derived-test-node)

        (let [output1 (node-methods/flush-output derived-test-node 1)]
          (cljtest/is (= (count output1) 2))
          (let [out1 (nth output1 0)
                out2 (nth output1 1)]
            (cljtest/is (= (channel-methods/get-type-tag out1) channel-types/IntegerT))
            (cljtest/is (= (channel-methods/get-type-tag out2) channel-types/IntegerT))
            (cljtest/is (= (channel-methods/get-field-value out1 number-channel-fields/value) 19))
            (cljtest/is (= (channel-methods/get-field-value out2 number-channel-fields/value) 24))))
        
        (node-methods/store derived-test-node 3 (channel-methods/create channel-types/IntegerT number-channel-fields/value 4))
        (node-methods/store derived-test-node 3 (channel-methods/create channel-types/IntegerT number-channel-fields/value 5))
        (node-methods/store derived-test-node 3 (channel-methods/create channel-types/IntegerT number-channel-fields/value 6))
        (node-methods/store derived-test-node 3 (channel-methods/create channel-types/IntegerT number-channel-fields/value 7))
        
        (let [input-buffer3 @((derived-test-node base-node-fields/inputs-buffers) 3)]
          (cljtest/is (= (count input-buffer3) 4))
          (cljtest/is (= (channel-methods/get-type-tag (nth input-buffer3 0)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-type-tag (nth input-buffer3 1)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-type-tag (nth input-buffer3 2)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-type-tag (nth input-buffer3 3)) channel-types/IntegerT))
          (cljtest/is (= (channel-methods/get-field-value (nth input-buffer3 0) number-channel-fields/value) 4))
          (cljtest/is (= (channel-methods/get-field-value (nth input-buffer3 1) number-channel-fields/value) 5))
          (cljtest/is (= (channel-methods/get-field-value (nth input-buffer3 2) number-channel-fields/value) 6))
          (cljtest/is (= (channel-methods/get-field-value (nth input-buffer3 3) number-channel-fields/value) 7)))
        
        (node-methods/execute derived-test-node)
        
        (let [output1 (node-methods/flush-output derived-test-node 1)
              output2 (node-methods/flush-output derived-test-node 2)]
          (cljtest/is (= (count output1) 1))
          (cljtest/is (= (count output2) 1))
          (let [out11 (nth output1 0)
                out21 (nth output2 0)]
            (cljtest/is (= (channel-methods/get-type-tag out11) channel-types/IntegerT))
            (cljtest/is (= (channel-methods/get-type-tag out21) channel-types/IntegerT))
            (cljtest/is (= (channel-methods/get-field-value out11 number-channel-fields/value) 160))
            (cljtest/is (= (channel-methods/get-field-value out21 number-channel-fields/value) 86))))))))

(cljtest/run-tests 'node-create)
