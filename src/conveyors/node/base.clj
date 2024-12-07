(ns conveyors.node.base
  (:require [conveyors.utils              :as utils]
            [conveyors.node.channels      :as node-channels]
            [conveyors.node.keywords      :as node-keywords]
            [conveyors.node.hierarchy     :as node-hierarchy]
            [conveyors.node.type-keywords :as node-type-keywords]))


(dosync (alter node-hierarchy/tree #(assoc % node-type-keywords/Node {node-keywords/T       node-type-keywords/Node
                                                                      node-keywords/super   nil
                                                                      node-keywords/inputs  ()
                                                                      node-keywords/outputs ()
                                                                      node-keywords/func    (fn [])
                                                                      node-keywords/fields  ()})))

; TODO: связать параметры func с типами каналов
(defn validate-func [func]
  (fn? func))

(defn append-node-hierarchy [new-type]
  {:pre [(contains? @node-hierarchy/tree (new-type node-keywords/super))
         (every? (partial utils/in-list? node-channels/types)
                 (concat (new-type node-keywords/inputs) (new-type node-keywords/outputs)))
         (validate-func (new-type node-keywords/func))]}
  (dosync (alter node-hierarchy/tree #(assoc % (new-type node-keywords/T) new-type))))


(defmacro define-node-type [name & sections] "\"sections\" is a sequence of pairs"
  `(let [sec-map#    (hash-map ~@sections)
         super#      (or (sec-map# node-keywords/super) node-type-keywords/Node)
         super-desc# (@node-hierarchy/tree super#)]
     (append-node-hierarchy {node-keywords/T       ~name
                             node-keywords/super   super#
                             node-keywords/inputs  (or (sec-map# node-keywords/inputs)  (super-desc# node-keywords/inputs))
                             node-keywords/outputs (or (sec-map# node-keywords/outputs) (super-desc# node-keywords/outputs))
                             node-keywords/func    (or (sec-map# node-keywords/func)    (super-desc# node-keywords/func))
                             node-keywords/fields  (or (sec-map# node-keywords/fields)  (super-desc# node-keywords/fields))})
     (assert (utils/lists-equal? node-keywords/keywords (keys (node-hierarchy/tree ~name)))
             "Not all node-keywords are added to define-node-type macro")))