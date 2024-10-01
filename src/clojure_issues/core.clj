(ns clojure-issues.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure-issues.issues :as issues]))

(defn load-config []
  (let [config-file (io/resource "config.edn")]
    (when config-file
      (edn/read-string (slurp config-file)))))

(def config (load-config))

(defn prompt [message]
  (println message)
  (flush)
  (read-line))

(defn choose-url []
  (let [owner-repo (prompt "Digite o owner e o nome do repositório no formato 'owner/repo' (ou pressione Enter para usar o repositório padrão):")
        url        (str "https://api.github.com/repos/" owner-repo "/issues?state=all")]
    (if (empty? owner-repo)
      (:default-github-api-url config)
      url)))

(defn menu []
  (println "Escolha uma opção:")
  (println "1. Listar todas as issues")
  (println "2. Filtrar issues por estado (open/closed)")
  (println "3. Filtrar issues por label")
  (println "4. Filtrar issues por autor")
  (println "5. Agrupar issues por label")
  (println "6. Agrupar issues por autor")
  (println "7. Ordenar issues por quantidade de comentários (menos para mais)")
  (println "8. Ordenar issues por data de criação (mais antigas para mais recentes)")
  (println "9. Obter estatísticas de issues")
  (println "0. Sair")
  (prompt "Selecione uma opção:"))

(defn ^:private only-titles [issues]
  (doseq [title (issues/list-titles issues)]
    (println title)))

(defn ^:private only-titles-maps [issues]
  (-> (into {}
            (map (fn [[user issues-list]]
                   [user (map #(select-keys % [:title]) issues-list)])
                 issues))
      println))

(defn run []
  (let [issues-url (choose-url)
        issues     (issues/fetch-all-pages-raw issues-url)]
    (loop []
      (let [choice (menu)]
        (cond
          (= choice "1") (do
                           (only-titles issues)
                           (println)
                           (recur))

          (= choice "2") (do
                           (let [state (prompt "Digite 'open' para issues abertas ou 'closed' para fechadas:")]
                             (only-titles (issues/filter-by-state issues state)))
                           (println)
                           (recur))

          (= choice "3") (do
                           (let [label (prompt "Digite o nome do label:")]
                             (only-titles (issues/filter-by-label issues label)))
                           (println)
                           (recur))

          (= choice "4") (do
                           (let [author (prompt "Digite o user do autor:")]
                             (only-titles (issues/filter-by-author issues author)))
                           (println)
                           (recur))

          (= choice "5") (do
                           (only-titles-maps (issues/group-by-labels issues))
                           (println)
                           (recur))

          (= choice "6") (do
                           (only-titles-maps (issues/group-by-author issues))
                           (println)
                           (recur))

          (= choice "7") (do
                           (only-titles (issues/order-by-comments issues))
                           (println)
                           (recur))

          (= choice "8") (do
                           (only-titles (issues/order-by-created_at issues))
                           (println)
                           (recur))

          (= choice "9") (do
                           (println {:issues-abertas        (issues/count-issues-by-state issues "open")
                                     :issues-fechadas       (issues/count-issues-by-state issues "closed")
                                     :comentarios-por-issue (issues/count-comments-by-issue issues)})
                           (println)
                           (recur))

          (= choice "0") (println "Saindo...")

          :else (do
                  (println "Opção inválida!")
                  (recur)))))))

(defn -main [& args]
  (run))
