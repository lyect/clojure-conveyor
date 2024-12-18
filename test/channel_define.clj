(ns channel-define
  (:require [clojure.test                              :as cljtest]
            [blocks.channel.base                       :as channel-base]
            [blocks.channel.definitions.channel.def    :as base-channel-def]
            [blocks.channel.definitions.channel.fields :as base-channel-fields]
            [blocks.channel.exceptions                 :as channel-exceptions]
            [blocks.channel.hierarchy                  :as channel-hierarchy]
            [blocks.channel.properties                 :as channel-properties]
            [blocks.channel.types                      :as channel-types]
            [utils]))


(intern 'blocks.channel.types 'types-list [channel-types/ChannelT ::TestChannel ::DerivedTestChannel])

(dosync (base-channel-def/define-base-channel))

(cljtest/deftest channel-define
  (cljtest/testing "Channel definition test"
    (channel-base/define-channel-type ::TestChannel
      channel-properties/fields '(::h ::w))
    (cljtest/is (channel-types/defined? ::TestChannel))
    (let [test-channel-type (channel-hierarchy/tree ::TestChannel)]
      (cljtest/is (=                  (test-channel-type channel-properties/type-name)  ::TestChannel))
      (cljtest/is (=                  (test-channel-type channel-properties/super-name) channel-types/ChannelT))
      (cljtest/is (utils/lists-equal? (test-channel-type channel-properties/fields)     (concat (list ::h ::w) base-channel-fields/fields-list))))
    (dosync (alter channel-hierarchy/tree #(dissoc % ::TestChannel)))
    (cljtest/is (and
                 (channel-hierarchy/tree channel-types/ChannelT)
                 (= (count @channel-hierarchy/tree) 1)))))

(cljtest/deftest channel-define-derived
  (cljtest/testing "Derived channel definition test"
    (channel-base/define-channel-type ::TestChannel
                                      channel-properties/fields '(::h ::w))
    (channel-base/define-channel-type ::DerivedTestChannel
                                      channel-properties/super-name ::TestChannel
                                      channel-properties/fields     '(::c))
    (cljtest/is (channel-types/defined? ::DerivedTestChannel))
    (let [derived-test-channel-type (channel-hierarchy/tree ::DerivedTestChannel)]
      (cljtest/is (=                  (derived-test-channel-type channel-properties/type-name)  ::DerivedTestChannel))
      (cljtest/is (=                  (derived-test-channel-type channel-properties/super-name) ::TestChannel))
      (cljtest/is (utils/lists-equal? (derived-test-channel-type channel-properties/fields)     (concat (list ::h ::w ::c) base-channel-fields/fields-list))))
    (dosync (alter channel-hierarchy/tree #(dissoc % ::TestChannel)))
    (dosync (alter channel-hierarchy/tree #(dissoc % ::DerivedTestChannel)))
    (cljtest/is (and
                 (channel-hierarchy/tree channel-types/ChannelT)
                 (= (count @channel-hierarchy/tree) 1)))))

(cljtest/deftest channel-define-type-undeclared
  (cljtest/testing "Channel with undeclared type definition test"
    (cljtest/is
     (try
       (channel-base/define-channel-type ::UndeclaredTestChannel
                                         channel-properties/fields ())
       (catch clojure.lang.ExceptionInfo e
         (if (and (= channel-exceptions/define-channel-type (-> e ex-data channel-exceptions/type-keyword))
                  (= channel-exceptions/type-undeclared     (-> e ex-data channel-exceptions/cause-keyword)))
           true
           false))))
    (cljtest/is (and
                 (channel-hierarchy/tree channel-types/ChannelT)
                 (= (count @channel-hierarchy/tree) 1)))))

(cljtest/deftest channel-define-super-undeclared
  (cljtest/testing "Channel with undeclared super definition test"
    (cljtest/is
     (try
       (channel-base/define-channel-type ::TestChannel
                                         channel-properties/super-name ::UndeclaredTestChannel
                                         channel-properties/fields     ())
       (catch clojure.lang.ExceptionInfo e
         (if (and (= channel-exceptions/define-channel-type (-> e ex-data channel-exceptions/type-keyword))
                  (= channel-exceptions/super-undeclared    (-> e ex-data channel-exceptions/cause-keyword)))
           true
           false))))
    (cljtest/is (and
                 (channel-hierarchy/tree channel-types/ChannelT)
                 (= (count @channel-hierarchy/tree) 1)))))

(cljtest/deftest channel-define-super-undefined
  (cljtest/testing "Channel with undefined super definition test"
    (cljtest/is
     (try
       (channel-base/define-channel-type ::DerivedTestChannel
                                         channel-properties/super-name ::TestChannel
                                         channel-properties/fields     ())
       (catch clojure.lang.ExceptionInfo e
         (if (and (= channel-exceptions/define-channel-type (-> e ex-data channel-exceptions/type-keyword))
                  (= channel-exceptions/super-undefined     (-> e ex-data channel-exceptions/cause-keyword)))
           true
           false))))
    (cljtest/is (and
                 (channel-hierarchy/tree channel-types/ChannelT)
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
                 (channel-hierarchy/tree channel-types/ChannelT)
                 (= (count @channel-hierarchy/tree) 1)))))

(cljtest/deftest channel-define-super-intersected-fields
  (cljtest/testing "Derived channel with fields intersecting super's fields definition test"
    (channel-base/define-channel-type ::TestChannel
                                      channel-properties/fields '(::h ::w))
    (cljtest/is
     (try
       (channel-base/define-channel-type ::DerivedTestChannel
                                         channel-properties/super-name ::TestChannel
                                         channel-properties/fields     '(::h))
       (catch clojure.lang.ExceptionInfo e
         (if (and (= channel-exceptions/define-channel-type       (-> e ex-data channel-exceptions/type-keyword))
                  (= channel-exceptions/super-fields-intersection (-> e ex-data channel-exceptions/cause-keyword)))
           true
           false))))
    (dosync (alter channel-hierarchy/tree #(dissoc % ::TestChannel)))
    (cljtest/is (and
                 (channel-hierarchy/tree channel-types/ChannelT)
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
                 (channel-hierarchy/tree channel-types/ChannelT)
                 (= (count @channel-hierarchy/tree) 1)))))


(cljtest/run-tests 'channel-define)
