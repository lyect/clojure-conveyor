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

(def get-vertex-property          ::type-get-vertex-property)
(def create                       ::type-create)
(def get-node-input-channel-type  ::get-node-input-channel-type)
(def get-node-output-channel-type ::type-get-node-output-channel-type)
(def get-node-inputs-count        ::type-get-node-inputs-count)
(def get-node-outputs-count       ::type-get-node-outputs-count)
(def set-node-input-connected     ::type-set-node-input-connected)
(def node-input-connected         ::type-node-input-connected)
(def all-node-inputs-connected    ::type-all-node-inputs-connected)
(def set-node-output-connected    ::type-set-node-output-connected)
(def node-output-connected        ::type-node-output-connected)
(def all-node-outputs-connected   ::type-all-node-outputs-connected)
(def start                        ::type-start)

(def ^:private type-list [get-vertex-property
                          create
                          get-node-input-channel-type
                          get-node-output-channel-type
                          get-node-inputs-count
                          get-node-outputs-count
                          set-node-input-connected
                          node-input-connected
                          all-node-inputs-connected
                          set-node-output-connected
                          node-output-connected
                          all-node-outputs-connected
                          start])

;; +----------------------+
;; |                      |
;; |   EXCEPTION CAUSES   |
;; |                      |
;; +----------------------+

(def not-vertex                ::cause-not-vertex)
(def not-node                  ::cause-not-node)
(def vertex-properties-missing ::cause-vertex-properties-missing)

(def ^:private cause-list [not-vertex
                           not-node
                           vertex-properties-missing])

;; +---------------------------------------------------+
;; |                                                   |
;; |   EXCEPTION CAUSES AND TYPES AND CORRESPONDENCE   |
;; |                                                   |
;; +---------------------------------------------------+

(def ^:private types-causes-correspondence {get-vertex-property          [not-vertex]
                                            create                       [not-node
                                                                          vertex-properties-missing]
                                            get-node-input-channel-type  [not-vertex]
                                            get-node-output-channel-type [not-vertex]
                                            get-node-inputs-count        [not-vertex]
                                            get-node-outputs-count       [not-vertex]
                                            set-node-input-connected     [not-vertex]
                                            node-input-connected         [not-vertex]
                                            all-node-inputs-connected    [not-vertex]
                                            set-node-output-connected    [not-vertex]
                                            node-output-connected        [not-vertex]
                                            all-node-outputs-connected   [not-vertex]
                                            start                        [not-vertex]})

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
