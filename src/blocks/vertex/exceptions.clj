(ns blocks.vertex.exceptions
  (:require [utils]))


;; +-----------------------------+
;; |                             |
;; |   EXCEPTION INFO KEYWORDS   |
;; |                             |
;; +-----------------------------+

(def type-keyword  ::keyword-type-keyword)
(def cause-keyword ::keyword-cause-keyword)

;; +---------------------+
;; |                     |
;; |   EXCEPTION TYPES   |
;; |                     |
;; +---------------------+

(def get-vertex-property    ::type-get-vertex-property)
(def create                 ::type-create)
(def get-node-input         ::type-get-node-input)
(def get-node-output        ::type-get-node-output)
(def get-node-inputs-count  ::type-get-node-inputs-count)
(def get-node-outputs-count ::type-get-node-outputs-count)
(def set-input-connected    ::type-set-input-connected)
(def input-connected        ::type-input-connected)
(def all-inputs-connected   ::type-all-inputs-connected)
(def set-output-connected   ::type-set-output-connected)
(def output-connected       ::type-output-connected)
(def all-outputs-connected  ::type-all-outputs-connected)
(def run                    ::type-run)
(def start                  ::type-start)

(def ^:private type-list [get-vertex-property
                          create
                          get-node-input
                          get-node-output
                          get-node-inputs-count
                          get-node-outputs-count
                          set-input-connected
                          input-connected
                          all-inputs-connected
                          set-output-connected
                          output-connected
                          all-outputs-connected
                          run
                          start])

;; +----------------------+
;; |                      |
;; |   EXCEPTION CAUSES   |
;; |                      |
;; +----------------------+

(def not-vertex                ::cause-not-vertex)
(def not-node                  ::cause-not-node)
(def vertex-properties-missing ::cause-vertex-properties-missing)
(def unknown-channel           ::cause-unknown-channel)

(def ^:private cause-list [not-vertex
                           not-node
                           vertex-properties-missing
                           unknown-channel])

;; +---------------------------------------------------+
;; |                                                   |
;; |   EXCEPTION CAUSES AND TYPES AND CORRESPONDENCE   |
;; |                                                   |
;; +---------------------------------------------------+

(def ^:private types-causes-correspondence {get-vertex-property    [not-vertex]
                                            create                 [not-node
                                                                    vertex-properties-missing]
                                            get-node-input         [not-vertex]
                                            get-node-output        [not-vertex]
                                            get-node-inputs-count  [not-vertex]
                                            get-node-outputs-count [not-vertex]
                                            set-input-connected    [not-vertex]
                                            input-connected        [not-vertex]
                                            all-inputs-connected   [not-vertex]
                                            set-output-connected   [not-vertex]
                                            output-connected       [not-vertex]
                                            all-outputs-connected  [not-vertex]
                                            run                    [unknown-channel]
                                            start                  [not-vertex]})

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
