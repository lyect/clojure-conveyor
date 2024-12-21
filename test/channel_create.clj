(ns channel-create
  (:require [clojure.test                           :as cljtest]
            [blocks.channel.base                    :as channel-base]
            [blocks.channel.definitions.channel.def :as base-channel-def]
            [blocks.channel.exceptions              :as channel-exceptions]
            [blocks.channel.methods                 :as channel-methods]
            [blocks.channel.properties              :as channel-properties]
            [blocks.channel.types                   :as channel-types]
            [utils]))


(intern 'blocks.channel.types 'types-list [channel-types/ChannelT ::TestChannel1 ::TestChannel2 ::TestChannel3 ::UndefinedChannel])

(base-channel-def/define-base-channel)

(channel-base/define-channel-type ::TestChannel1
                                  channel-properties/fields '(::h ::w))
(channel-base/define-channel-type ::TestChannel2
                                  channel-properties/fields '(::x ::y))
(channel-base/define-channel-type ::TestChannel3
                                  channel-properties/super-name ::TestChannel1
                                  channel-properties/fields     '(::c))


(cljtest/deftest channel-creation
  (cljtest/testing "Channel creation test"
    (let [test-channel (dosync (channel-methods/create ::TestChannel1 ::h 1 ::w 2))]
      (cljtest/is (=                  (channel-methods/get-channel-type-name  test-channel) ::TestChannel1))
      (cljtest/is (=                  (channel-methods/get-channel-super-name test-channel) channel-types/ChannelT))
      (cljtest/is (utils/lists-equal? (channel-methods/get-channel-fields     test-channel) (list ::h ::w)))

      (cljtest/is (= (channel-methods/get-channel-field test-channel ::h) 1))
      (cljtest/is (= (channel-methods/get-channel-field test-channel ::w) 2)))))

(cljtest/deftest channel-creation-derived
  (cljtest/testing "Derived channel creation test"
    (let [derived-test-channel (dosync (channel-methods/create ::TestChannel3 ::h 1 ::w 2 ::c 3))]
      (cljtest/is (=                  (channel-methods/get-channel-type-name  derived-test-channel) ::TestChannel3))
      (cljtest/is (=                  (channel-methods/get-channel-super-name derived-test-channel) ::TestChannel1))
      (cljtest/is (utils/lists-equal? (channel-methods/get-channel-fields     derived-test-channel) (list ::h ::w ::c)))

      (cljtest/is (= (channel-methods/get-channel-field derived-test-channel ::h) 1))
      (cljtest/is (= (channel-methods/get-channel-field derived-test-channel ::w) 2)))))

(cljtest/deftest channel-creation-undeclared-type
  (cljtest/testing "Channel with undeclared type fields creation test"
    (cljtest/is
     (try
       (dosync (channel-methods/create ::UndeclaredChannel))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= channel-exceptions/create          (-> e ex-data channel-exceptions/type-keyword))
                  (= channel-exceptions/type-undeclared (-> e ex-data channel-exceptions/cause-keyword)))
           true
           false))))))

(cljtest/deftest channel-creation-undefined-type
  (cljtest/testing "Channel with undefined type fields creation test"
    (cljtest/is
     (try
       (dosync (channel-methods/create ::UndefinedChannel))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= channel-exceptions/create         (-> e ex-data channel-exceptions/type-keyword))
                  (= channel-exceptions/type-undefined (-> e ex-data channel-exceptions/cause-keyword)))
           true
           false))))))

(cljtest/deftest channel-creation-abstract
  (cljtest/testing "Abstract channel creation test"
    (cljtest/is
     (try
       (dosync (channel-methods/create channel-types/ChannelT))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= channel-exceptions/create            (-> e ex-data channel-exceptions/type-keyword))
                  (= channel-exceptions/abstract-creation (-> e ex-data channel-exceptions/cause-keyword)))
           true
           false))))))

(cljtest/deftest channel-creation-duplicating-fields
  (cljtest/testing "Channel with duplicating fields creation test"
    (cljtest/is
     (try
       (dosync (channel-methods/create ::TestChannel1 ::h 1 ::h 2))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= channel-exceptions/create             (-> e ex-data channel-exceptions/type-keyword))
                  (= channel-exceptions/duplicating-fields (-> e ex-data channel-exceptions/cause-keyword)))
           true
           false))))))

(cljtest/deftest channel-creation-missing-fields
  (cljtest/testing "Channel with missing fields creation test"
    (cljtest/is
     (try
       (dosync (channel-methods/create ::TestChannel1 ::h 1))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= channel-exceptions/create         (-> e ex-data channel-exceptions/type-keyword))
                  (= channel-exceptions/missing-fields (-> e ex-data channel-exceptions/cause-keyword)))
           true
           false))))))

(cljtest/deftest channel-creation-excess-fields
  (cljtest/testing "Channel with excess fields creation test"
    (cljtest/is
     (try
       (dosync (channel-methods/create ::TestChannel1 ::h 1 ::w 2 ::x 3))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= channel-exceptions/create        (-> e ex-data channel-exceptions/type-keyword))
                  (= channel-exceptions/excess-fields (-> e ex-data channel-exceptions/cause-keyword)))
           true
           false))))))


(cljtest/run-tests 'channel-create)
