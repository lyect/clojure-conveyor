(ns channel-define
  (:require [clojure.test              :as cljtest]
            [blocks.channel.base       :as channel-base]
            [blocks.channel.exceptions :as channel-exceptions]
            [blocks.channel.hierarchy  :as channel-hierarchy]
            [blocks.channel.methods    :as channel-methods]
            [blocks.channel.properties :as channel-properties]
            [blocks.channel.types      :as channel-types]
            [blocks.utils              :as utils]))


(intern 'blocks.channel.types 'types-list [::TestChannel ::DerivedTestChannel])


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

(cljtest/deftest channel-redefine
  (cljtest/testing "Channel redefinition test"
    (channel-base/define-channel-type ::TestChannel
      channel-properties/fields '(::h ::w))
    (cljtest/is
     (try
       (channel-base/define-channel-type ::TestChannel
         channel-properties/fields '(::x ::y))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= channel-exceptions/define-channel-type (-> e ex-data channel-exceptions/type-keyword))
                  (= channel-exceptions/type-defined        (-> e ex-data channel-exceptions/cause-keyword)))
           true
           false))))
    (dosync (alter channel-hierarchy/tree #(dissoc % ::TestChannel)))
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
                  (= channel-exceptions/duplicating-fields  (-> e ex-data channel-exceptions/cause-keyword)))
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
