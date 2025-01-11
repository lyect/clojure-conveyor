(ns blocks.node.properties)


(def label          ::property-label)
(def super-type-tag ::property-super-type-tag)
(def inputs         ::property-inputs)
(def outputs        ::property-outputs)
(def links          ::property-links)
(def inputs-map     ::property-inputs-tag-type-map)
(def outputs-map    ::property-outputs-tag-type-map)
(def links-map      ::property-links-map)
(def fields-tags    ::property-fields-tags)

(def properties-list [label
                      super-type-tag
                      inputs
                      outputs
                      links
                      inputs-map
                      outputs-map
                      links-map
                      fields-tags])
