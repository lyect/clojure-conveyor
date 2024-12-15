(ns blocks.vertex.exceptions
  (:require [utils]))


;; +-----------------------------+
;; |                             |
;; |   EXCEPTION INFO KEYWORDS   |
;; |                             |
;; +-----------------------------+

(def type-keyword  ::vertexexceptionkeyword-type-keyword)
(def cause-keyword ::vertexexceptionkeyword-cause-keyword)

;; +---------------------+
;; |                     |
;; |   EXCEPTION TYPES   |
;; |                     |
;; +---------------------+

(def get-vertex-property ::vertexexceptiontype-get-vertex-property)
(def create              ::vertexexceptiontype-create)
(def get-input           ::vertexexceptiontype-get-input)
(def get-output          ::vertexexceptiontype-get-output)
(def set-input-used      ::vertexexceptiontype-set-input-used)
(def reset-input-used    ::vertexexceptiontype-reset-input-used)
(def input-used          ::vertexexceptiontype-input-used)
(def all-inputs-used     ::vertexexceptiontype-all-inputs-used)
(def set-output-used     ::vertexexceptiontype-set-output-used)
(def reset-output-used   ::vertexexceptiontype-reset-output-used)
(def output-used         ::vertexexceptiontype-output-used)
(def all-outputs-used    ::vertexexceptiontype-all-outputs-used)
(def set-input-ready     ::vertexexceptiontype-set-input-ready)
(def reset-input-ready   ::vertexexceptiontype-reset-input-ready)
(def input-ready         ::vertexexceptiontype-input-ready)
(def all-inputs-ready    ::vertexexceptiontype-all-inputs-ready)
(def get-inputs-count    ::vertexexceptiontype-get-inputs-count)
(def get-outputs-count   ::vertexexceptiontype-get-outputs-count)

(def ^:private type-list [get-vertex-property
                          create
                          get-input
                          get-output
                          set-input-used
                          reset-input-used
                          input-used
                          all-inputs-used
                          set-output-used
                          reset-output-used
                          output-used
                          all-outputs-used
                          set-input-ready
                          reset-input-ready
                          input-ready
                          all-inputs-ready
                          get-inputs-count
                          get-outputs-count])

;; +----------------------+
;; |                      |
;; |   EXCEPTION CAUSES   |
;; |                      |
;; +----------------------+

(def not-vertex                ::vertexexceptioncause-not-vertex)
(def not-node                  ::vertexexceptioncause-not-node)
(def already-set               ::vertexexceptioncause-already-set)
(def already-reset             ::vertexexceptioncause-already-reset)
(def vertex-properties-missing ::vertexexceptioncause-vertex-properties-missing)

(def ^:private cause-list [not-vertex
                           not-node
                           already-set
                           already-reset
                           vertex-properties-missing])

;; +---------------------------------------------------+
;; |                                                   |
;; |   EXCEPTION CAUSES AND TYPES AND CORRESPONDENCE   |
;; |                                                   |
;; +---------------------------------------------------+

(def ^:private types-causes-correspondence {get-vertex-property [not-vertex]
                                            create              [not-node
                                                                 vertex-properties-missing]
                                            get-input           [not-vertex]
                                            get-output          [not-vertex]
                                            set-input-used      [not-vertex
                                                                 already-set]
                                            reset-input-used    [not-vertex
                                                                 already-reset]
                                            input-used          [not-vertex]
                                            all-inputs-used     [not-vertex]
                                            set-output-used     [not-vertex
                                                                 already-set]
                                            reset-output-used   [not-vertex
                                                                 already-reset]
                                            output-used         [not-vertex]
                                            all-outputs-used    [not-vertex]
                                            set-input-ready     [not-vertex
                                                                 already-set]
                                            reset-input-ready   [not-vertex
                                                                 already-reset]
                                            input-ready         [not-vertex]
                                            all-inputs-ready    [not-vertex]
                                            get-inputs-count    [not-vertex]
                                            get-outputs-count   [not-vertex]})

;; +---------------------------+
;; |                           |
;; |   EXCEPTION CONSTRUCTOR   |
;; |                           |
;; +---------------------------+

(defn construct
  "Construct exception with _type_, _cause_ and _message_"
  [type cause message]
  {:pre [(utils/in-list? type-list                          type)
         (utils/in-list? cause-list                         cause)
         (utils/in-list? (types-causes-correspondence type) cause)]}
  (ex-info message {type-keyword  type
                    cause-keyword cause}))
