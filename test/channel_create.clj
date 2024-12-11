(ns channel-create
  (:require [clojure.test                 :as cljtest]
            [conveyors.channel.base       :as channel-base]
            [conveyors.channel.exceptions :as channel-exceptions]
            [conveyors.channel.methods    :as channel-methods]
            [conveyors.channel.properties :as channel-properties]
            [conveyors.channel.types      :as channel-types]
            [conveyors.utils              :as utils]))


(intern 'conveyors.channel.types 'types-list [::TestChannel1 ::TestChannel2 ::TestChannel3])

(channel-base/define-channel-type ::TestChannel1
                                  channel-properties/fields '(::h ::w))
(channel-base/define-channel-type ::TestChannel2
                                  channel-properties/fields '(::x ::y))
(channel-base/define-channel-type ::TestChannel3
                                  channel-properties/super  ::TestChannel1
                                  channel-properties/fields '(::c))


(cljtest/deftest channel-creation
  (cljtest/testing "Channel creation test"
    (let [test-channel (channel-methods/create ::TestChannel1 ::h 1 ::w 2)]
      (cljtest/is (=                  (channel-methods/get-channel-type   test-channel) ::TestChannel1))
      (cljtest/is (=                  (channel-methods/get-channel-super  test-channel) channel-types/Channel))
      (cljtest/is (utils/lists-equal? (channel-methods/get-channel-fields test-channel) '(::h ::w)))

      (cljtest/is (= (channel-methods/get-channel-field test-channel ::h) 1))
      (cljtest/is (= (channel-methods/get-channel-field test-channel ::w) 2)))))

(cljtest/deftest channel-creation-derived
  (cljtest/testing "Derived channel creation test"
    (let [derived-test-channel (channel-methods/create ::TestChannel3 ::h 1 ::w 2 ::c 3)]
      (cljtest/is (=                  (channel-methods/get-channel-type   derived-test-channel) ::TestChannel3))
      (cljtest/is (=                  (channel-methods/get-channel-super  derived-test-channel) ::TestChannel1))
      (cljtest/is (utils/lists-equal? (channel-methods/get-channel-fields derived-test-channel) '(::h ::w ::c)))

      (cljtest/is (= (channel-methods/get-channel-field derived-test-channel ::h) 1))
      (cljtest/is (= (channel-methods/get-channel-field derived-test-channel ::w) 2)))))

(cljtest/deftest channel-subtype
  (cljtest/testing "Channel subtype? method test"
    (let [test-channel (channel-methods/create ::TestChannel1 ::h 1 ::w 2)
          derived-test-channel (channel-methods/create ::TestChannel3 ::h 1 ::w 2 ::c 3)]
      (cljtest/is (channel-methods/have-subtype? derived-test-channel test-channel))
      (cljtest/is (channel-methods/have-subtype? test-channel test-channel))
      (cljtest/is (not (channel-methods/have-subtype? test-channel derived-test-channel))))))

(cljtest/deftest channel-creation-duplicating-fields
  (cljtest/testing "Channel with duplicating fields creation test"
    (cljtest/is
     (try
       (channel-methods/create ::TestChannel1 ::h 1 ::h 2)
       (catch clojure.lang.ExceptionInfo e
         (and (= channel-exceptions/create             (-> e ex-data channel-exceptions/type-keyword))
              (= channel-exceptions/duplicating-fields (-> e ex-data channel-exceptions/cause-keyword))))))))

(cljtest/deftest channel-creation-missing-fields
  (cljtest/testing "Channel with missing fields creation test"
    (cljtest/is
     (try
       (channel-methods/create ::TestChannel1 ::h 1)
       (catch clojure.lang.ExceptionInfo e
         (and (= channel-exceptions/create         (-> e ex-data channel-exceptions/type-keyword))
              (= channel-exceptions/missing-fields (-> e ex-data channel-exceptions/cause-keyword))))))))

(cljtest/deftest channel-creation-excess-fields
  (cljtest/testing "Channel with excess fields creation test"
    (cljtest/is
     (try
       (channel-methods/create ::TestChannel1 ::h 1 ::w 2 ::x 3)
       (catch clojure.lang.ExceptionInfo e
         (and (= channel-exceptions/create        (-> e ex-data channel-exceptions/type-keyword))
              (= channel-exceptions/excess-fields (-> e ex-data channel-exceptions/cause-keyword))))))))


(cljtest/run-tests 'channel-create)
