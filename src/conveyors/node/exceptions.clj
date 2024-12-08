(ns conveyors.node.exceptions
  (:require [conveyors.utils :as utils]))

(def type-keyword  ::nodeexceptionkeyword-type-keyword)
(def cause-keyword ::nodeexceptionkeyword-cause-keyword)


; Exception types
(def define-node-type ::nodeexceptiontype-define-node-type)
(def create           ::nodeexceptiontype-create)

(def types [define-node-type
            create])


; Exception causes
(def duplicating-fields        ::nodeexceptioncause-duplicating-fields)
(def super-fields-intersection ::nodeexceptioncause-super-fields-intersection)
(def node-properties-missing   ::nodeexceptioncause-node-properties-missing)
(def missing-fields            ::nodeexceptioncause-missing-fields)
(def excess-fields             ::nodeexceptioncause-excess-fields)
(def type-not-declared         ::nodeexceptioncause-type-not-declared)

(def causes [duplicating-fields
             super-fields-intersection
             node-properties-missing
             missing-fields
             excess-fields
             type-not-declared])


(def type-causes {define-node-type [duplicating-fields
                                    super-fields-intersection
                                    node-properties-missing
                                    type-not-declared]
                  create           [duplicating-fields
                                    missing-fields
                                    excess-fields]})


(defn construct
  [type cause message]
  {:pre [(utils/in-list? types              type)
         (utils/in-list? causes             cause)
         (utils/in-list? (type-causes type) cause)]}
  (ex-info message {type-keyword  type
                    cause-keyword cause}))