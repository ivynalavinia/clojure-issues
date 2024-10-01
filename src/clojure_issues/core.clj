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
  (let [url (prompt "Digite a URL da API do GitHub (ou pressione Enter para usar a URL padrão):")]
    (if (empty? url)
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

;Obter todas as issues
;Filtrar issues abertas/fechadas, por label e por autor
;Agrupamento de issues por label e por autor
;Ordenar issues pela quantidade de comentários e por data de criação
;Obter número de issues abertas ou fechadas
;Obter número de comentários por issue

(defn run []
  (let [issues-url (choose-url)
        issues     (issues/fetch-all-pages-raw issues-url)]
    (loop []
      (let [choice (menu)]
        (cond
          (= choice "1") (do
                           (println (issues/list-titles issues))
                           (recur))

          (= choice "2") (do
                           (let [state (prompt "Digite 'open' para issues abertas ou 'closed' para fechadas:")]
                             (println (issues/filter-by-state issues state)))
                           (recur))

          (= choice "3") (do
                           (let [label (prompt "Digite o nome do label:")]
                             (println (issues/filter-by-label issues label)))
                           (recur))

          (= choice "4") (do
                           (let [author (prompt "Digite o nome do autor:")]
                             (println (issues/filter-by-author issues author)))
                           (recur))

          (= choice "5") (do
                           (println (issues/group-by-labels issues))
                           (recur))

          (= choice "6") (do
                           (println (issues/group-by-author issues))
                           (recur))

          (= choice "7") (do
                           (println (issues/order-by-comments issues))
                           (recur))

          (= choice "8") (do
                           (println (issues/order-by-created_at issues))
                           (recur))

          (= choice "9") (do
                           (println {:open-issues        (issues/count-issues-by-state issues "open")
                                     :closed-issues      (issues/count-issues-by-state issues "closed")
                                     :comments-per-issue (issues/count-comments-by-issue issues)})
                           (recur))

          (= choice "0") (println "Saindo...")

          :else (do
                  (println "Opção inválida!")
                  (recur)))))))

(defn -main [& args]
  (run))
