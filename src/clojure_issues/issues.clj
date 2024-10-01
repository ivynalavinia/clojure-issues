(ns clojure-issues.issues
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure-issues.utils :as utils]
            [clojure.instant :refer [read-instant-date]])
  (:import [java.time Instant]))

(defn fetch-all-pages-raw [issues-url]
  (let [response (client/get issues-url)
        issues   (json/parse-string (:body response) true)
        next-url (-> response :links :next :href)]
    (if next-url
      (concat issues (fetch-all-pages-raw next-url))
      issues)))

(defn list-titles [issues]
  (map :title issues))

(defn filter-by-state [issues state]
  (filter #(= state (:state %)) issues))

(defn ^:private single-filter-by-label [issue label]
  (let [issue-labels (set (map :name (:labels issue)))]
    (contains? issue-labels label)))

(defn filter-by-label [issues label]
  (filter #(single-filter-by-label % label) issues))

(defn filter-by-author [issues author]
  (let [author-login (utils/compose :login :user)]
    (filter #(= (author-login %) author) issues)))

(defn group-by-labels [issues]
  (let [all-labels      (map :name (mapcat :labels issues))
        distinct-labels (utils/myDistinct all-labels)]
    (utils/fold (fn [acc label]
                  (assoc acc label
                             (filter (fn [issue]
                                       (some #(= label (:name %)) (:labels issue)))
                                     issues)))
                {}
                distinct-labels)))

(defn group-by-author [issues]
  (let [extract-author (fn [issue] (get-in issue [:user :login]))]
    (utils/groupBy issues extract-author)))

(defn order-by-comments [issues]
  (utils/orderBy issues :comments))

(defn ^:private parse-timestamps [coll attr]
  (map #(update % attr (fn [param] (-> param read-instant-date .getTime))) coll))

(defn ^:private revert-timestamps [coll attr]
  (map #(update % attr (fn [ms] (.toString (Instant/ofEpochMilli ms)))) coll))

(defn order-by-created_at [issues]
  (let [parsed-issues (parse-timestamps issues :created_at)
        ordered       (utils/orderBy parsed-issues :created_at)]
    (revert-timestamps ordered :created_at)))

(defn count-issues-by-state [issues state]
  (count (filter #(= state (:state %)) issues)))

(defn count-comments-by-issue [issues]
  (into {} (map (fn [issue]
                  [(:title issue) (str (:comments issue) " comentários")])
                issues)))
