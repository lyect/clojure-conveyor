(ns channel-define
  (:require [conveyors.utils           :as utils]
            [clojure.test              :as cljtest]
            [conveyors.channel.base    :as channel-base]
            [conveyors.channel.properties :as channel-properties]
            [conveyors.channel.methods    :as channel-methods]
            [conveyors.channel.types      :as channel-types]
            [conveyors.channel.exceptions :as channel-exceptions]
            [conveyors.channel.hierarchy  :as channel-hierarchy]))


(intern 'conveyors.channel.types 'types-list [::TestChannel ::DerivedTestChannel])


(cljtest/deftest channel-define
  (cljtest/testing "Channel definition test"
    (channel-base/define-channel-type ::TestChannel
      channel-properties/fields '(::h ::w))
    (cljtest/is (channel-methods/channel-type-defined? ::TestChannel))
    (let [test-channel-type (channel-hierarchy/tree ::TestChannel)]
      (cljtest/is (=                  (test-channel-type channel-properties/T)      ::TestChannel))
      (cljtest/is (=                  (test-channel-type channel-properties/super)  channel-types/Channel))
      (cljtest/is (utils/lists-equal? (test-channel-type channel-properties/fields) '(::h ::w))))
    (dosync (alter channel-hierarchy/tree #(dissoc % ::TestChannel)))
    (cljtest/is (and
                 (channel-hierarchy/tree channel-types/Channel)
                 (= (count @channel-hierarchy/tree) 1)))))

(cljtest/deftest channel-define-derived
  (cljtest/testing "Derived channel definition test"
    (channel-base/define-channel-type ::TestChannel
      channel-properties/fields '(::h ::w))
    (channel-base/define-channel-type ::DerivedTestChannel
      channel-properties/super  ::TestChannel
      channel-properties/fields '(::c))
    (cljtest/is (channel-methods/channel-type-defined? ::DerivedTestChannel))
    (let [derived-test-channel-type (channel-hierarchy/tree ::DerivedTestChannel)]
      (cljtest/is (=                  (derived-test-channel-type channel-properties/T)      ::DerivedTestChannel))
      (cljtest/is (=                  (derived-test-channel-type channel-properties/super)  ::TestChannel))
      (cljtest/is (utils/lists-equal? (derived-test-channel-type channel-properties/fields) '(::h ::w ::c))))
    (dosync (alter channel-hierarchy/tree #(dissoc % ::TestChannel)))
    (dosync (alter channel-hierarchy/tree #(dissoc % ::DerivedTestChannel)))
    (cljtest/is (and
                 (channel-hierarchy/tree channel-types/Channel)
                 (= (count @channel-hierarchy/tree) 1)))))

(cljtest/deftest channel-define-duplicated-fields
  (cljtest/testing "Channel with duplicated fields definition test"
    (cljtest/is
     (try
       (channel-base/define-channel-type ::TestChannel
         channel-properties/fields '(::h ::h))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= channel-exceptions/define-channel-type (-> e ex-data channel-exceptions/type-keyword))
                  (= channel-exceptions/duplicating-fields   (-> e ex-data channel-exceptions/cause-keyword)))
           true
           false))))
    (cljtest/is (and
                 (channel-hierarchy/tree channel-types/Channel)
                 (= (count @channel-hierarchy/tree) 1)))))

(cljtest/deftest channel-define-super-intersected-fields
  (cljtest/testing "Derived channel with fields intersecting super's fields definition test"
    (channel-base/define-channel-type ::TestChannel
      channel-properties/fields '(::h ::w))
    (cljtest/is
     (try
       (channel-base/define-channel-type ::DerivedTestChannel
         channel-properties/super  ::TestChannel
         channel-properties/fields '(::h))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= channel-exceptions/define-channel-type       (-> e ex-data channel-exceptions/type-keyword))
                  (= channel-exceptions/super-fields-intersection (-> e ex-data channel-exceptions/cause-keyword)))
           true
           false))))
    (dosync (alter channel-hierarchy/tree #(dissoc % ::TestChannel)))
    (cljtest/is (and
                 (channel-hierarchy/tree channel-types/Channel)
                 (= (count @channel-hierarchy/tree) 1)))))


(cljtest/run-tests 'channel-define)