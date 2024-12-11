(ns conveyor.methods)


(defn- get-forward-conveyor [conveyor]
  (nth conveyor 0))

(defn- get-backward-conveyor [conveyor]
  (nth conveyor 1))


(defn producer? [conveyor node]
  (contains? @(get-forward-conveyor conveyor) node))

(defn consumer? [conveyor node]
  (contains? @(get-backward-conveyor conveyor) node))
