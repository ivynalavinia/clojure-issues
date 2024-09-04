(ns clojure-issues.utils)

; Clojure possui sua própria implementação de distinct, por isso foi necessário renomear essa função
(defn myDistinct [coll attr]
  (->> coll
       (group-by #(get % attr))
       (vals)
       (map first)))

(defn groupBy [coll attr]
  (let [fn-or-key (if (fn? attr) attr (partial get attr))]
    (group-by fn-or-key coll)))

(defn orderBy [coll attr]
  (sort-by #(get % attr) coll))

(defn fold [reducer init coll]
  (reduce reducer init coll))

(defn compose [f1 f2]
  (fn [x] (f1 (f2 x))))
