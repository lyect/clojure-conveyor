(ns conveyors.node-definitions)


(defn in-list? [lst elem]   ;todo: move to utils module, if it will be
  (some #(= % elem) lst))


(def channel-types '(::INT ::PNG))  ;todo: create system of types

(def node-hierarchy (ref {}))

;аналог Object, не может быть определен штатно
(dosync (alter node-hierarchy #(assoc % :Node {::type :Node
                                               ::super nil
                                               ::inputs ()
                                               ::outputs ()
                                               ::func (fn [])
                                               ::fields ()})))

(defn append-node-hierarchy [new-type]
  {:pre [(contains? @node-hierarchy (new-type ::super))
         (every? (partial in-list? channel-types)
                 (concat (new-type ::inputs) (new-type ::outputs)))
         (fn? (new-type ::func))]}                        ;; todo: связать параметры func с типами каналов
  (dosync (alter node-hierarchy #(assoc % (new-type ::type) new-type))))


(defmacro def-node-type [name & sections] "sections is sequence of pair"
  `(let [sec-map# (hash-map ~@sections)
         super# (or (sec-map# :super) :Node)
         super-desc# (@node-hierarchy super#)]
     (append-node-hierarchy {::type ~name
                             ::super super#
                             ::inputs (or (sec-map# :inputs) (super-desc# ::inputs))
                             ::outputs (or (sec-map# :outputs) (super-desc# ::outputs))
                             ::func (or (sec-map# :func) (super-desc# ::func))
                             ::fields (or (sec-map# :fields) (super-desc# ::fields))})))


(defn has-field? [type field]
  (in-list? ((@node-hierarchy type) ::fields) field))

(defn node-type [node]
  (node ::type))

(defn super-type [type]
  ((@node-hierarchy type) ::super))

(defn get-property [node prop]
  ((@node-hierarchy (node-type node)) prop))

(defn execute [node & params]
  (apply (get-property node ::func) params))

(defn get-field [obj field]
  (obj field))

(defn set-field! [obj field value]
  (dosync (alter obj #(assoc % field value))))


(defn create-node [type & fields]
  (:pre [(contains? @node-hierarchy type)])
  (let [fields-map (apply hash-map fields)
        obj (ref {})]
    (doseq [key-value fields-map]
      (when (has-field? type (first key-value))
        (set-field! obj (first key-value) (second key-value))))
    (set-field! obj ::type type)))


(def-node-type :TestNode
               :inputs '(::INT ::PNG)
               :outputs '(::PNG)
               :func (fn [x pict] (+ pict x))
               :fields '(::f1 ::f2 ::f3))

(let [test-node (create-node :TestNode ::f1 8 ::f2 "str" ::f3 :x)])
