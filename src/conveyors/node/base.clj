(ns conveyors.node.base
  (:require [conveyors.utils           :as utils]
            [conveyors.node.channels   :as node-channels]
            [conveyors.node.properties :as node-properties]
            [conveyors.node.hierarchy  :as node-hierarchy]
            [conveyors.node.types      :as node-types]))


(dosync (alter node-hierarchy/tree #(assoc % node-types/Node {node-properties/T       node-types/Node
                                                              node-properties/super   nil
                                                              node-properties/inputs  ()
                                                              node-properties/outputs ()
                                                              node-properties/func    (fn [])
                                                              node-properties/fields  ()})))

; TODO: связать параметры func с типами каналов
(defn validate-func [func]
  (fn? func))

(defn append-node-hierarchy [new-type]
  {:pre [(contains? @node-hierarchy/tree (new-type node-properties/super))
         (every? (partial utils/in-list? node-channels/types)
                 (concat (new-type node-properties/inputs) (new-type node-properties/outputs)))
         (validate-func (new-type node-properties/func))]}
  (dosync (alter node-hierarchy/tree #(assoc % (new-type node-properties/T) new-type))))


(defmacro define-node-type [name & sections] "\"sections\" is a sequence of pairs"
  `(let [sec-map#    (hash-map ~@sections)
         super#      (or (sec-map# node-properties/super) node-types/Node)
         super-desc# (@node-hierarchy/tree super#)]
     (append-node-hierarchy {node-properties/T       ~name
                             node-properties/super   super#
                             node-properties/inputs  (or (sec-map# node-properties/inputs)  (super-desc# node-properties/inputs))
                             node-properties/outputs (or (sec-map# node-properties/outputs) (super-desc# node-properties/outputs))
                             node-properties/func    (or (sec-map# node-properties/func)    (super-desc# node-properties/func))
                             node-properties/fields  (or (sec-map# node-properties/fields)  (super-desc# node-properties/fields))})
     (assert (utils/lists-equal? node-properties/properties-list (keys (node-hierarchy/tree ~name)))
             "Not all node-properties are added to define-node-type macro")))