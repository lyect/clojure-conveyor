(ns conveyors.channel.exceptions
  (:require [conveyors.utils :as utils]))

(def type-keyword  ::channelexceptionkeyword-type-keyword)
(def cause-keyword ::channelexceptionkeyword-cause-keyword)


; Exception types
(def define-channel-type ::channelexceptiontype-define-channel-type)
(def create              ::channelexceptiontype-create)

(def types [define-channel-type
            create])


; Exception causes
(def duplicating-fields         ::channelexceptioncause-duplicating-fields)
(def super-fields-intersection  ::channelexceptioncause-super-fields-intersection)
(def channel-properties-missing ::channelexceptioncause-channel-properties-missing)
(def missing-fields             ::channelexceptioncause-missing-fields)
(def excess-fields              ::channelexceptioncause-excess-fields)
(def type-not-declared          ::channelexceptioncause-type-not-declared)

(def causes [duplicating-fields
             super-fields-intersection
             channel-properties-missing
             missing-fields
             excess-fields
             type-not-declared])


(def type-causes {define-channel-type [duplicating-fields
                                       super-fields-intersection
                                       channel-properties-missing
                                       type-not-declared]
                  create              [duplicating-fields
                                       missing-fields
                                       excess-fields]})


(defn construct
  [type cause message]
  {:pre [(utils/in-list? types              type)
         (utils/in-list? causes             cause)
         (utils/in-list? (type-causes type) cause)]}
  (ex-info message {type-keyword  type
                    cause-keyword cause}))