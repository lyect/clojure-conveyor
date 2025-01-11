(ns channel-create
  (:require [blocks.channel.definitions.channel.def :as base-channel-def]
            [blocks.channel.methods                 :as channel-methods]
            [blocks.channel.properties              :as channel-properties]
            [blocks.channel.types                   :as channel-types]
            [clojure.test                           :as cljtest]
            [utils]))


(intern 'blocks.channel.types 'types-tags-list          [channel-types/ChannelT ::TestChannel1 ::TestChannel2])
(intern 'blocks.channel.types 'abstract-types-tags-list [])

(base-channel-def/define)

(channel-types/define "TestChannel1" ::TestChannel1
                      channel-properties/fields-tags [::h ::w])
(channel-types/define "TestChannel2" ::TestChannel2
                      channel-properties/super-type-tag ::TestChannel1
                      channel-properties/fields-tags    [::c])


(cljtest/deftest channel-creation
  (cljtest/testing "Channel creation test"
    (let [test-channel (dosync (channel-methods/create ::TestChannel1 ::h 1 ::w 2))]
      (cljtest/is (= (channel-methods/get-type-tag    test-channel) ::TestChannel1))
      (cljtest/is (= (channel-methods/get-field-value test-channel ::h) 1))
      (cljtest/is (= (channel-methods/get-field-value test-channel ::w) 2)))))

(cljtest/deftest channel-creation-derived
  (cljtest/testing "Derived channel creation test"
    (let [test-channel (dosync (channel-methods/create ::TestChannel2 ::h 1 ::w 2 ::c 3))]
      (cljtest/is (= (channel-methods/get-type-tag    test-channel) ::TestChannel2))
      (cljtest/is (= (channel-methods/get-field-value test-channel ::h) 1))
      (cljtest/is (= (channel-methods/get-field-value test-channel ::w) 2))
      (cljtest/is (= (channel-methods/get-field-value test-channel ::c) 3)))))


(cljtest/run-tests 'channel-create)
