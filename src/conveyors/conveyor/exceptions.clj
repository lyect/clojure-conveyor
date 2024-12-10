(ns conveyors.conveyor.exceptions
  (:require [conveyors.utils :as utils]))


(def type-keyword  ::conveyorexceptionkeyword-type-keyword)
(def cause-keyword ::conveyorexceptionkeyword-cause-keyword)


; Exception types
(def build ::conveyorexceptiontype-build)

(def types [build])


; Exception causes
(def incorrect-edge ::conveyorexceptioncause-incorrect-edge)
(def incompatible-channels ::conveyorexceptioncause-incompatible-channels)
(def non-existent-channel  ::conveyorexceptioncause-non-existent-channel)
(def twice-use ::conveyorexceptioncause-double-use)

(def causes [incorrect-edge
             incompatible-channels
             non-existent-channel
             twice-use])


(def type-causes {build [incorrect-edge
                         incompatible-channels
                         non-existent-channel
                         twice-use]})


(defn construct [type cause message]
  {:pre [(utils/in-list? types              type)
         (utils/in-list? causes             cause)
         (utils/in-list? (type-causes type) cause)]}
  (ex-info message {type-keyword  type
                    cause-keyword cause}))
