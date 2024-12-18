(ns blocks.node.exceptions
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

(def define-node-type  ::type-define-node-type)
(def create            ::type-create)
(def get-node-property ::type-get-node-property)
(def get-node-field    ::type-get-node-field)
(def get-node-name     ::type-get-node-name)
(def store             ::type-store)
(def execute           ::type-execute)

(def ^:private type-list [define-node-type
                          create
                          get-node-property
                          get-node-field
                          get-node-name
                          store
                          execute])

;; +----------------------+
;; |                      |
;; |   EXCEPTION CAUSES   |
;; |                      |
;; +----------------------+

(def duplicating-fields         ::cause-duplicating-fields)
(def super-fields-intersection  ::cause-super-fields-intersection)
(def node-properties-missing    ::cause-node-properties-missing)
(def missing-fields             ::cause-missing-fields)
(def excess-fields              ::cause-excess-fields)
(def type-undeclared            ::cause-type-undeclared)
(def type-defined               ::cause-type-defined)
(def super-undeclared           ::cause-super-undeclared)
(def super-undefined            ::cause-super-undefined)
(def inputs-unvalidated         ::cause-inputs-unvalidated)
(def outputs-unvalidated        ::cause-outputs-unvalidated)
(def function-unvalidated       ::cause-function-unvalidated)
(def not-node                   ::cause-not-node)
(def type-undefined             ::cause-type-undefined)
(def unknown-field              ::cause-unknown-field)
(def abstract-creation          ::cause-abstract-creation)
(def function-undefined         ::cause-function-undefined)
(def ready-validator-undefined  ::cause-ready-validator-undefined)
(def inputs-validator-undefined ::cause-inputs-validator-undefined)
(def inputs-undefined           ::cause-inputs-undefined)
(def outputs-undefined          ::cause-outputs-undefined)
(def no-buffer-under-index      ::cause-no-buffer-under-index)

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
                           function-undefined
                           ready-validator-undefined
                           inputs-validator-undefined
                           inputs-undefined
                           outputs-undefined
                           no-buffer-under-index])

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
                                                              ready-validator-undefined
                                                              inputs-validator-undefined
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
                                            store             [not-node
                                                               no-buffer-under-index]
                                            execute           [inputs-unvalidated]})

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
