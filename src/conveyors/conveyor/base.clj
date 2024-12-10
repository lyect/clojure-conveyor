(ns conveyors.conveyor.base
  (:require [conveyors.channel.methods :as channel-methods]
            [conveyors.conveyor.exceptions :as conveyor-exception]
            [conveyors.node.methods :as node-methods]))


(def build-exception (partial conveyor-exception/construct conveyor-exception/build))


(defn have-one-type? [ch1 ch2]
  (= (channel-methods/get-channel-type ch1) (channel-methods/get-channel-type ch2)))

(defn free-channel? [node-in-conv num-ch]
  (or (nil? node-in-conv)
      (not (contains? @node-in-conv num-ch))))


(defn build-conveyor [& edges]
  "\"edges\" - sequence of (<node-producer> <number output channel> <node-consumer> <number input channel>)"
  (let [conv-forward (ref {})
        conv-backward (ref {})]
    (doseq [edge edges]
      (when-not (= (count edge) 4)
        (throw (build-exception conveyor-exception/incorrect-edge
                                (str "Tried to add incorrect edge: " edge ". Must be 4 parameters"))))
      (let [node-producer (nth edge 0)
            num-ch-producer (dec (nth edge 1))
            ch-producer (try (nth (node-methods/get-node-outputs node-producer) num-ch-producer)
                             (catch Exception _
                               (throw (build-exception conveyor-exception/non-existent-channel
                                                       (str "Tried to use non-existent channel producer: " edge)))))
            node-consumer (nth edge 2)
            num-ch-consumer (dec (nth edge 3))
            ch-consumer (try (nth (node-methods/get-node-inputs node-consumer) num-ch-consumer)
                             (catch Exception _
                               (throw (build-exception conveyor-exception/non-existent-channel
                                                       (str "Tried to use non-existent channel consumer: " edge)))))]
        (when-not (have-one-type? ch-producer ch-consumer)
          (throw (build-exception conveyor-exception/incompatible-channels
                                  (str "Tried to connect incompatible channels: " edge))))
        (when-not (and (free-channel? (@conv-forward node-producer) num-ch-producer)
                       (free-channel? (@conv-backward node-consumer) num-ch-consumer))
          (throw (build-exception conveyor-exception/twice-use
                                  (str "Tried to use channel twice: " edge))))
        (dosync
          (when-not (contains? @conv-forward node-producer)
            (alter conv-forward #(assoc % node-producer (ref {}))))
          (alter (@conv-forward node-producer) #(assoc % num-ch-producer [node-consumer num-ch-consumer]))
          (when-not (contains? @conv-backward node-consumer)
            (alter conv-backward #(assoc % node-consumer (ref {}))))
          (alter (@conv-backward node-consumer) #(assoc % num-ch-consumer [node-producer num-ch-producer])))))
    [conv-forward conv-backward]))
