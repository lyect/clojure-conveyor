(ns channel-define
  (:require [blocks.channel.definitions.channel.def    :as base-channel-def]
            [blocks.channel.definitions.channel.fields :as base-channel-fields]
            [blocks.channel.hierarchy                  :as channel-hierarchy]
            [blocks.channel.properties                 :as channel-properties]
            [blocks.channel.types                      :as channel-types]
            [clojure.test                              :as cljtest]
            [utils]))


(intern 'blocks.channel.types 'types-tags-list          [channel-types/ChannelT ::TestChannel ::DerivedTestChannel])
(intern 'blocks.channel.types 'abstract-types-tags-list [])


(base-channel-def/define)


(cljtest/deftest channel-define
  (cljtest/testing "Channel definition test"
    (channel-types/define "TestChannel" ::TestChannel
                          channel-properties/fields-tags [::h ::w])
    (cljtest/is (channel-types/defined? ::TestChannel))
    (let [test-channel-type (channel-hierarchy/tree ::TestChannel)]
      (cljtest/is (=                  (test-channel-type channel-properties/super-type-tag) channel-types/ChannelT))
      (cljtest/is (=                  (test-channel-type channel-properties/label)          "TestChannel"))
      (cljtest/is (utils/lists-equal? (test-channel-type channel-properties/fields-tags)    (into base-channel-fields/tags-list [::h ::w]))))
    (dosync (alter channel-hierarchy/tree #(dissoc % ::TestChannel)))
    (cljtest/is (and
                 (channel-hierarchy/tree channel-types/ChannelT)
                 (= (count @channel-hierarchy/tree) 1)))))

(cljtest/deftest channel-define-derived
  (cljtest/testing "Derived channel definition test"
    (channel-types/define "TestChannel" ::TestChannel
                          channel-properties/label       "TestChannel"
                          channel-properties/fields-tags [::h ::w])
    (channel-types/define "DerivedTestChannel" ::DerivedTestChannel
                          channel-properties/super-type-tag ::TestChannel
                          channel-properties/label          "DerivedTestChannel"
                          channel-properties/fields-tags    [::c])
    (cljtest/is (channel-types/defined? ::DerivedTestChannel))
    (let [derived-test-channel-type (channel-hierarchy/tree ::DerivedTestChannel)]
      (cljtest/is (=                  (derived-test-channel-type channel-properties/super-type-tag) ::TestChannel))
      (cljtest/is (=                  (derived-test-channel-type channel-properties/label)          "DerivedTestChannel"))
      (cljtest/is (utils/lists-equal? (derived-test-channel-type channel-properties/fields-tags)    (into base-channel-fields/tags-list [::h ::w ::c]))))
    (dosync (alter channel-hierarchy/tree #(dissoc % ::TestChannel)))
    (dosync (alter channel-hierarchy/tree #(dissoc % ::DerivedTestChannel)))
    (cljtest/is (and
                 (channel-hierarchy/tree channel-types/ChannelT)
                 (= (count @channel-hierarchy/tree) 1)))))


(cljtest/run-tests 'channel-define)
