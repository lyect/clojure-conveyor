(ns node-define
  (:require
   [blocks.channel.types                   :as channel-types]
   [blocks.node.definitions.node.def       :as base-node-def]
   [blocks.node.definitions.node.fields    :as base-node-fields]
   [blocks.node.hierarchy                  :as node-hierarchy]
   [blocks.node.input.methods              :as node-input-methods]
   [blocks.node.link.methods               :as node-link-methods]
   [blocks.node.output.methods             :as node-output-methods]
   [blocks.node.properties                 :as node-properties]
   [blocks.node.types                      :as node-types]
   [clojure.test                           :as cljtest]
   [utils]))


(intern 'blocks.node.types 'types-tags-list [node-types/NodeT ::TestNode ::DerivedTestNode])
(intern 'blocks.node.types 'abstract-types-tags-list [])

(base-node-def/define)


(cljtest/deftest node-define
  (cljtest/testing "Node definition test"
    (let [input1  (node-input-methods/create 1 channel-types/ImageT)
          input2  (node-input-methods/create 2 channel-types/FloatT)
          output  (node-output-methods/create ::TestNodeOutput channel-types/JpegT)
          handler (fn [_ _] (constantly nil))
          link    (node-link-methods/create [1 2] [::TestNodeOutput] handler)]
      (node-types/define "TestNode" ::TestNode
        node-properties/inputs      [input1 input2]
        node-properties/outputs     [output]
        node-properties/links       [link]
        node-properties/fields-tags [::f1 ::f2 ::f3])
      (cljtest/is (node-types/defined? ::TestNode))
      (let [test-node-type (node-hierarchy/tree ::TestNode)]
        (cljtest/is (=                  (test-node-type node-properties/label) "TestNode"))
        (cljtest/is (=                  (test-node-type node-properties/super-type-tag) node-types/NodeT))
        (cljtest/is (utils/lists-equal? (test-node-type node-properties/inputs)         [input1 input2]))
        (cljtest/is (utils/lists-equal? (test-node-type node-properties/outputs)        [output]))
        (cljtest/is (utils/lists-equal? (test-node-type node-properties/links)          [link]))
        (cljtest/is (=                  (test-node-type node-properties/inputs-map)     {1 channel-types/ImageT
                                                                                         2 channel-types/FloatT}))
        (cljtest/is (=                  (test-node-type node-properties/outputs-map)    {::TestNodeOutput channel-types/JpegT}))
        (cljtest/is (=                  (test-node-type node-properties/links-map)      {[1 2] [handler [::TestNodeOutput]]}))
        (cljtest/is (utils/lists-equal? (test-node-type node-properties/fields-tags)    (into base-node-fields/tags-list [::f1 ::f2 ::f3])))))
    (dosync (alter node-hierarchy/tree #(dissoc % ::TestNode)))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))

(cljtest/deftest node-define-derived
  (cljtest/testing "Derived node definition test"
    (let [input1  (node-input-methods/create 1 channel-types/ImageT)
          input2  (node-input-methods/create 2 channel-types/FloatT)
          input3  (node-input-methods/create 3 channel-types/FloatT)
          output1 (node-output-methods/create 1 channel-types/JpegT)
          output2 (node-output-methods/create 2 channel-types/NumberT)
          handler (fn [_ _] (constantly nil))
          link1   (node-link-methods/create [1 2] [1] handler)
          link2   (node-link-methods/create [3] [2] handler)]
      (node-types/define "TestNode" ::TestNode
        node-properties/inputs      [input1 input2]
        node-properties/outputs     [output1]
        node-properties/links       [link1]
        node-properties/fields-tags [::f1 ::f2 ::f3])
      (node-types/define "DerivedTestNode" ::DerivedTestNode
        node-properties/super-type-tag ::TestNode
        node-properties/inputs         [input3]
        node-properties/outputs        [output2]
        node-properties/links          [link2]
        node-properties/fields-tags    [::f4])
      (cljtest/is (node-types/defined? ::DerivedTestNode))
      (let [derived-test-node-type (node-hierarchy/tree ::DerivedTestNode)]
        (cljtest/is (=                  (derived-test-node-type node-properties/label) "DerivedTestNode"))
        (cljtest/is (=                  (derived-test-node-type node-properties/super-type-tag) ::TestNode))
        (cljtest/is (utils/lists-equal? (derived-test-node-type node-properties/inputs)         [input1 input2 input3]))
        (cljtest/is (utils/lists-equal? (derived-test-node-type node-properties/outputs)        [output1 output2]))
        (cljtest/is (utils/lists-equal? (derived-test-node-type node-properties/links)          [link1 link2]))
        (cljtest/is (=                  (derived-test-node-type node-properties/inputs-map)     {1 channel-types/ImageT
                                                                                                 2 channel-types/FloatT
                                                                                                 3 channel-types/FloatT}))
        (cljtest/is (=                  (derived-test-node-type node-properties/outputs-map)    {1 channel-types/JpegT
                                                                                                 2 channel-types/NumberT}))
        (cljtest/is (=                  (derived-test-node-type node-properties/links-map)      {[1 2] [handler [1]]
                                                                                                 [3]   [handler [2]]}))
        (cljtest/is (utils/lists-equal? (derived-test-node-type node-properties/fields-tags)    (into base-node-fields/tags-list [::f1 ::f2 ::f3 ::f4])))))
    (dosync (alter node-hierarchy/tree #(dissoc % ::TestNode)))
    (dosync (alter node-hierarchy/tree #(dissoc % ::DerivedTestNode)))
    (cljtest/is (and
                 (node-hierarchy/tree node-types/NodeT)
                 (= (count @node-hierarchy/tree) 1)))))

(cljtest/run-tests 'node-define)
