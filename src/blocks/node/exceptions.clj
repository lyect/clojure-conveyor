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

(def define                  ::type-define)
(def get-property            ::type-get-property)
(def abstract                ::type-abstract)
(def get-field-value         ::type-get-field-value)
(def create                  ::type-create)
(def set-required-input-load ::type-set-required-input-load)
(def store                   ::type-store)
(def get-type-tag            ::type-get-type-tag)
(def get-label               ::type-get-label)
(def flush-output            ::type-flush-output)
(def execute                 ::type-execute)
(def reserve-label           ::type-reserve-label)
(def get-fields              ::type-get-fields)

(def ^:private type-list [define
                          get-property
                          abstract
                          get-field-value
                          create
                          set-required-input-load
                          store
                          get-type-tag
                          get-label
                          flush-output
                          execute
                          reserve-label
                          get-fields])

;; +----------------------+
;; |                      |
;; |   EXCEPTION CAUSES   |
;; |                      |
;; +----------------------+

(def type-undeclared                           ::cause-type-undeclared)
(def type-defined                              ::cause-type-defined)
(def super-type-undeclared                     ::cause-super-type-undeclared)
(def super-type-undefined                      ::cause-super-type-undefined)
(def inputs-not-vector                         ::cause-inputs-not-vector)
(def duplicated-inputs-tags                    ::cause-duplicated-inputs-tags)
(def outputs-not-vector                        ::cause-outputs-not-vector)
(def duplicated-outputs-tags                   ::cause-duplicated-outputs-tags)
(def links-not-vector                          ::cause-links-not-vector)
(def duplicated-links-inputs-tags              ::cause-duplicated-links-inputs-tags)
(def fields-tags-not-vector                    ::cause-fields-tags-not-vector)
(def duplicated-fields-tags                    ::cause-duplicated-fields-tags)
(def super-type-inputs-tags-intersection       ::cause-super-type-inputs-tags-intersection)
(def super-type-outputs-tags-intersection      ::cause-super-type-outputs-tags-intersection)
(def super-type-links-inputs-tags-intersection ::cause-super-type-links-inputs-tags-intersection)
(def super-type-fields-tags-intersection       ::cause-super-type-fields-tags-intersection)
(def type-undefined                            ::cause-type-undefined)
(def not-node                                  ::cause-not-node)
(def unknown-field-tag                         ::cause-unknown-field-tag)
(def type-abstract                             ::cause-type-abstract)
(def missed-fields-tags                        ::cause-missed-fields-tags)
(def excess-fields-tags                        ::cause-excess-fields-tags)
(def unknown-input-tag                         ::cause-unknown-input-tag)
(def small-min-load                            ::cause-small-min-load)
(def label-reserved                            ::cause-label-reserved)

(def ^:private cause-list [type-undeclared
                           type-defined
                           super-type-undeclared
                           super-type-undefined
                           inputs-not-vector
                           duplicated-inputs-tags
                           outputs-not-vector
                           duplicated-outputs-tags
                           links-not-vector
                           duplicated-links-inputs-tags
                           fields-tags-not-vector
                           duplicated-fields-tags
                           super-type-inputs-tags-intersection
                           super-type-outputs-tags-intersection
                           super-type-links-inputs-tags-intersection
                           super-type-fields-tags-intersection
                           type-undefined
                           not-node
                           unknown-field-tag
                           type-abstract
                           missed-fields-tags
                           excess-fields-tags
                           unknown-input-tag
                           small-min-load
                           label-reserved])

;; +---------------------------------------------------+
;; |                                                   |
;; |   EXCEPTION CAUSES AND TYPES AND CORRESPONDENCE   |
;; |                                                   |
;; +---------------------------------------------------+

(def ^:private types-causes-correspondence {define                  [type-undeclared
                                                                     type-defined
                                                                     super-type-undeclared
                                                                     super-type-undefined
                                                                     inputs-not-vector
                                                                     duplicated-inputs-tags
                                                                     outputs-not-vector
                                                                     duplicated-outputs-tags
                                                                     links-not-vector
                                                                     duplicated-links-inputs-tags
                                                                     fields-tags-not-vector
                                                                     duplicated-fields-tags
                                                                     super-type-inputs-tags-intersection
                                                                     super-type-outputs-tags-intersection
                                                                     super-type-links-inputs-tags-intersection
                                                                     super-type-fields-tags-intersection]
                                            get-property            [type-undeclared
                                                                     type-undefined]
                                            abstract                [type-undeclared
                                                                     type-undefined]
                                            get-field-value         [not-node
                                                                     unknown-field-tag]
                                            create                  [type-undeclared
                                                                     type-undefined
                                                                     type-abstract
                                                                     duplicated-fields-tags
                                                                     missed-fields-tags
                                                                     excess-fields-tags]
                                            set-required-input-load [not-node
                                                                     unknown-input-tag
                                                                     small-min-load]
                                            store                   [not-node]
                                            get-type-tag            [not-node]
                                            get-label               [not-node]
                                            flush-output            [not-node]
                                            execute                 [not-node]
                                            get-fields              [not-node]
                                            reserve-label           [label-reserved]})

;; +---------------------------+
;; |                           |
;; |   EXCEPTION CONSTRUCTOR   |
;; |                           |
;; +---------------------------+

(defn construct
  [type cause message]
  {:pre [(utils/in-list? type-list                          type)
         (utils/in-list? cause-list                         cause)
         (utils/in-list? (types-causes-correspondence type) cause)]}
  (ex-info message {type-keyword  type
                    cause-keyword cause}))
