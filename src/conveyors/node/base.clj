(ns conveyors.node.base
  (:require [clojure.set               :as cljset]
            [conveyors.channel.methods :as channel-methods]
            [conveyors.channel.types   :as channel-types]
            [conveyors.node.exceptions :as node-exceptions]
            [conveyors.node.hierarchy  :as node-hierarchy]
            [conveyors.node.properties :as node-properties]
            [conveyors.node.types      :as node-types]
            [conveyors.utils           :as utils]))


(dosync (alter node-hierarchy/tree #(assoc % node-types/Node {node-properties/T       node-types/Node
                                                              node-properties/super   nil
                                                              node-properties/inputs  ()
                                                              node-properties/outputs ()
                                                              node-properties/func    (fn [])
                                                              node-properties/fields  ()})))

; TODO: связать параметры func с типами каналов
(defn- validate-func [func]
  (fn? func))

(defn- validate-inputs [inputs]
  (reduce
   #(and %1 (utils/in-list? channel-types/types-list %2))
   true
   (map #(channel-methods/get-channel-type %1) inputs)))

(defn- validate-outputs [outputs]
  (reduce
   #(and %1 (utils/in-list? channel-types/types-list %2))
   true
   (map #(channel-methods/get-channel-type %1) outputs)))

(defn append-node-hierarchy [new-node-type]
  {:pre [(contains? @node-hierarchy/tree (new-node-type node-properties/super))
         (validate-inputs  (new-node-type node-properties/inputs))
         (validate-outputs (new-node-type node-properties/outputs))
         (validate-func    (new-node-type node-properties/func))]}
  (dosync (alter node-hierarchy/tree #(assoc % (new-node-type node-properties/T) new-node-type))))


(defmacro define-node-type [name & sections] "\"sections\" is a sequence of pairs"
  `(let [sec-map#          (hash-map ~@sections)
         super#            (or (sec-map# node-properties/super) node-types/Node)
         super-desc#       (@node-hierarchy/tree super#)
         sec-fields#       (sec-map# node-properties/fields)
         super-fields#     (super-desc# node-properties/fields)
         sec-fields-set#   (set sec-fields#)
         super-fields-set# (set super-fields#)]
     (when-not (utils/in-list? node-types/types-list ~name)
       (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/type-not-declared
                                         (str "Type " ~name " is not declared"))))
     (when-not (<= (count sec-fields#) (count sec-fields-set#))
       (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/duplicating-fields
                                         (str "Fields of " ~name " are duplicated"))))
     (when-not (empty? (cljset/intersection sec-fields-set# super-fields-set#))
       (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/super-fields-intersection
                                         (str "Fields of " ~name " intersect with fields of " (super-desc# node-properties/T)))))
     (append-node-hierarchy {node-properties/T       ~name
                             node-properties/super   super#
                             node-properties/inputs  (or     (sec-map# node-properties/inputs)  (super-desc# node-properties/inputs))
                             node-properties/outputs (or     (sec-map# node-properties/outputs) (super-desc# node-properties/outputs))
                             node-properties/func    (or     (sec-map# node-properties/func)    (super-desc# node-properties/func))
                             node-properties/fields  (concat sec-fields# super-fields#)})
     (when-not (utils/lists-equal? node-properties/properties-list (keys (node-hierarchy/tree ~name)))
       (do
         (dosync (alter node-hierarchy/tree #(dissoc % ~name)))
         (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/node-properties-missing
                                           "Not all node-properties are added to define-node-type macro"))))))
