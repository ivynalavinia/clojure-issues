(ns clojure-issues.utils)

; Clojure possui sua própria implementação de distinct, por isso foi necessário renomear essa função
(defn myDistinct
  ([coll]
   (myDistinct coll #{}))
  ([coll seen]
   (if (empty? coll)
     []
     (let [first-elem (first coll)]
       (if (contains? seen first-elem)
         (recur (rest coll) seen)
         (cons first-elem (myDistinct (rest coll) (conj seen first-elem))))))))

(defn groupBy [coll f]
  (reduce (fn [ret x]
            (let [k (if (fn? f) (f x) (get x f))]
              (if (nil? k)
                ret
                (assoc ret k (conj (get ret k []) x)))))
          {}
          coll))

(defn ^:private merge-two [left right attr]
  (cond
    (empty? left) right
    (empty? right) left
    :else (let [l (first left)
                r (first right)]
            (if (<= (get l attr) (get r attr))
              (cons l (merge-two (rest left) right attr))
              (cons r (merge-two left (rest right) attr))))))

(defn ^:private merge-sort [coll attr]
  (if (<= (count coll) 1)
    coll
    (let [mid   (quot (count coll) 2)
          left  (subvec coll 0 mid)
          right (subvec coll mid)]
      (merge-two (merge-sort left attr) (merge-sort right attr) attr))))

(defn orderBy [coll attr]
  (merge-sort (vec coll) attr))

(defn fold [f initial-value coll]
  (if (empty? coll)
    initial-value
    (recur f (f initial-value (first coll)) (rest coll))))

(defn compose [f1 f2]
  (fn [x] (f1 (f2 x))))
