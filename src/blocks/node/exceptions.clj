(ns blocks.node.exceptions
  (:require [utils]))


;; +-----------------------------+
;; |                             |
;; |   EXCEPTION INFO KEYWORDS   |
;; |                             |
;; +-----------------------------+

(def type-keyword  ::nodeexceptionkeyword-type-keyword)
(def cause-keyword ::nodeexceptionkeyword-cause-keyword)

;; +---------------------+
;; |                     |
;; |   EXCEPTION TYPES   |
;; |                     |
;; +---------------------+

(def define-node-type  ::nodeexceptiontype-define-node-type)
(def create            ::nodeexceptiontype-create)
(def get-node-property ::nodeexceptiontype-get-node-property)
(def get-node-field    ::nodeexceptiontype-get-node-field)
(def get-node-name     ::nodeexceptiontype-get-node-name)
(def execute           ::nodeexceptiontype-execute)

(def ^:private type-list [define-node-type
                          create
                          get-node-property
                          get-node-field
                          get-node-name
                          execute])

;; +----------------------+
;; |                      |
;; |   EXCEPTION CAUSES   |
;; |                      |
;; +----------------------+

(def duplicating-fields        ::nodeexceptioncause-duplicating-fields)
(def super-fields-intersection ::nodeexceptioncause-super-fields-intersection)
(def node-properties-missing   ::nodeexceptioncause-node-properties-missing)
(def missing-fields            ::nodeexceptioncause-missing-fields)
(def excess-fields             ::nodeexceptioncause-excess-fields)
(def type-undeclared           ::nodeexceptioncause-type-undeclared)
(def type-defined              ::nodeexceptioncause-type-defined)
(def super-undeclared          ::nodeexceptioncause-super-undeclared)
(def super-undefined           ::nodeexceptioncause-super-undefined)
(def inputs-unvalidated        ::nodeexceptioncause-inputs-unvalidated)
(def outputs-unvalidated       ::nodeexceptioncause-outputs-unvalidated)
(def function-unvalidated      ::nodeexceptioncause-function-unvalidated)
(def not-node                  ::nodeexceptioncause-not-node)
(def type-undefined            ::nodeexceptioncause-type-undefined)
(def unknown-field             ::nodeexceptioncause-unknown-field)
(def abstract-creation         ::nodeexceptioncause-abstract-creation)
(def input-not-channel         ::nodeexceptioncause-input-not-channel)
(def input-different-type      ::nodeexceptioncause-input-different-type)
(def output-not-channel        ::nodeexceptioncause-output-not-channel)
(def output-different-type     ::nodeexceptioncause-output-different-type)
(def function-undefined        ::nodeexceptioncause-function-undefined)
(def inputs-undefined          ::nodeexceptioncause-inputs-undefined)
(def outputs-undefined         ::nodeexceptioncause-outputs-undefined)

(def ^:private cause-list [duplicating-fields
                           super-fields-intersection
                           node-properties-missing
                           missing-fields
                           excess-fields
                           type-undeclared
                           type-defined
                           super-undeclared
                           super-undefined
                           inputs-unvalidated
                           outputs-unvalidated
                           function-unvalidated
                           not-node
                           type-undefined
                           unknown-field
                           abstract-creation
                           input-not-channel
                           input-different-type
                           output-not-channel
                           output-different-type
                           function-undefined
                           inputs-undefined
                           outputs-undefined])

;; +---------------------------------------------------+
;; |                                                   |
;; |   EXCEPTION CAUSES AND TYPES AND CORRESPONDENCE   |
;; |                                                   |
;; +---------------------------------------------------+

(def ^:private types-causes-correspondence {define-node-type [duplicating-fields
                                                              super-fields-intersection
                                                              node-properties-missing
                                                              type-undeclared
                                                              type-defined
                                                              super-undeclared
                                                              super-undefined
                                                              inputs-unvalidated
                                                              outputs-unvalidated
                                                              function-unvalidated
                                                              function-undefined
                                                              inputs-undefined
                                                              outputs-undefined]
                                            create           [duplicating-fields
                                                              missing-fields
                                                              excess-fields
                                                              type-undeclared
                                                              type-undefined
                                                              abstract-creation]
                                            get-node-property [not-node]
                                            get-node-field    [not-node
                                                               unknown-field]
                                            get-node-name     [not-node]
                                            execute           [not-node
                                                               input-not-channel
                                                               input-different-type
                                                               output-not-channel
                                                               output-different-type]})

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
