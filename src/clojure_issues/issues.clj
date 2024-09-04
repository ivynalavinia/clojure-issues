(ns clojure-issues.issues
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure-issues.utils :as utils]))

(defn fetch-raw [issues-url]
  (let [response (client/get issues-url)
        issues   (json/parse-string (:body response) true)
        next-url (-> response :links :next :href)]
    (if next-url
      (concat issues (fetch-raw next-url))
      issues)))

(defn list-titles [issues]
  (map :title issues))

(defn single-filter-by-labels [issue labels]
  (let [issue-labels (set (map :name (:labels issue)))]
    (some #(contains? issue-labels %) labels)))

(defn filter-by-labels [issues labels]
  (filter #(single-filter-by-labels % labels) issues))

(defn filter-by-author [issues author]
  (let [author-login (utils/compose :login :user)]
    (filter #(= (author-login %) author) issues)))

(defn group-by-labels [issues]
  (let [all-labels (map :name (mapcat :labels issues))]
    (let [distinct-labels (utils/myDistinct all-labels :name)]
      (utils/fold (fn [acc label]
              (assoc acc label
                         (filter (fn [issue]
                                   (some #(= label (:name %)) (:labels issue)))
                                 issues)))
            {}
            distinct-labels))))


(defn group-by-author [issues]
  (let [extract-author (fn [issue] (get-in issue [:user :login]))]
    (utils/groupBy issues extract-author)))

(defn order-by-criteria [issues criteria]
  (utils/orderBy issues criteria))

(defn count-issues [issues]
  (utils/fold (fn [acc issue]
          (update acc (:state issue) (fnil inc 0)))
        {}
        issues))

(defn count-comments-by-issue [issues]
  (into {} (map (fn [issue]
                  [(:html_url issue) (:comments issue)])
                issues)))

;Obter todas as issues
;Filtrar issues abertas/fechadas, por label e por autor
;Agrupamento de issues por label e por autor
;Ordenar issues pela quantidade de comentários e por data de criação
;Obter número de issues abertas ou fechadas
;Obter número de comentários por issue
