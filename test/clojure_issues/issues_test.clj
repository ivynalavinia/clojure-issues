(ns clojure-issues.issues-test
  (:require [clojure.test :refer :all]
            [clojure-issues.issues :refer :all]
            [clj-http.client :as client]
            [cheshire.core :as json]))

(def all-issues [{:html_url   "https://github.com/user/repository/issues/4"
                  :labels     []
                  :comments   0
                  :state      "closed"
                  :title      "Issue test 4"
                  :user       {:login "usuario1"}
                  :created_at "2024-09-27T18:18:55Z"}
                 {:html_url   "https://github.com/user/repository/issues/3"
                  :labels     []
                  :comments   2
                  :state      "open"
                  :title      "Issue test 3"
                  :user       {:login "usuario"}
                  :created_at "2024-09-25T13:21:31Z"}
                 {:html_url   "https://github.com/user/repository/issues/2"
                  :labels     [{:name "bug"}]
                  :comments   10
                  :state      "open"
                  :title      "Issue test 2"
                  :user       {:login "usuario"}
                  :created_at "2024-09-25T13:21:18Z"}
                 {:html_url   "https://github.com/user/repository/issues/1"
                  :labels     [{:name "bug"}
                               {:name "good first issue"}]
                  :comments   5
                  :state      "open"
                  :title      "Issue test 1"
                  :user       {:login "usuario"}
                  :created_at "2024-09-25T13:20:59Z"}])

(def url-page-1 "https://api.github.com/repos/user/repository/issues?state=all")
(def url-page-2 "https://api.github.com/repos/user/repository/issues?state=all&page=2")

(deftest test-fetch-all-pages-raw
  (testing "fetch-all-pages-raw deve retornar todos os issues de todas as páginas"
    (let [mock-response-page-1 {:body  (json/generate-string (take 2 all-issues))
                                :links {:next {:href url-page-2}}}
          mock-response-page-2 {:body  (json/generate-string (drop 2 all-issues))
                                :links {:next nil}}]

      (with-redefs [client/get (fn [url & _]
                                 (condp = url
                                   url-page-1 mock-response-page-1
                                   url-page-2 mock-response-page-2))]
        (let [result (fetch-all-pages-raw url-page-1)]
          (is (= result
                 all-issues)))))))

(deftest test-list-titles
  (testing "list-titles deve retornar uma lista com os títulos das issues"
    (is (= (list-titles all-issues)
           ["Issue test 4" "Issue test 3" "Issue test 2" "Issue test 1"]))))

(deftest test-filter-by-state
  (testing "filter-by-state deve retornar uma lista com as issues do estado informado"
    (is (= (filter-by-state all-issues "open")
           (rest all-issues)))
    (is (= (filter-by-state all-issues "closed")
           (take 1 all-issues)))))

(deftest test-filter-by-label
  (testing "filter-by-label deve retornar uma lista com as issues que possuem a label informada"
    (is (= (filter-by-label all-issues "bug")
           [{:html_url   "https://github.com/user/repository/issues/2"
             :labels     [{:name "bug"}]
             :comments   10
             :state      "open"
             :title      "Issue test 2"
             :user       {:login "usuario"}
             :created_at "2024-09-25T13:21:18Z"}
            {:html_url   "https://github.com/user/repository/issues/1"
             :labels     [{:name "bug"}
                          {:name "good first issue"}]
             :comments   5
             :state      "open"
             :title      "Issue test 1"
             :user       {:login "usuario"}
             :created_at "2024-09-25T13:20:59Z"}]))))

(deftest test-filter-by-author
  (testing "filter-by-author deve retornar uma lista com as issues do autor informado"
    (is (= (filter-by-author all-issues "usuario")
           (rest all-issues)))))

(deftest test-group-by-labels
  (testing "group-by-labels deve retornar um mapa com as issues agrupadas por label"
    (is (= (group-by-labels all-issues)
           {"bug"              [{:html_url   "https://github.com/user/repository/issues/2"
                                 :labels     [{:name "bug"}]
                                 :comments   10
                                 :state      "open"
                                 :title      "Issue test 2"
                                 :user       {:login "usuario"}
                                 :created_at "2024-09-25T13:21:18Z"}
                                {:html_url   "https://github.com/user/repository/issues/1"
                                 :labels     [{:name "bug"}
                                              {:name "good first issue"}]
                                 :comments   5
                                 :state      "open"
                                 :title      "Issue test 1"
                                 :user       {:login "usuario"}
                                 :created_at "2024-09-25T13:20:59Z"}]
            "good first issue" [{:html_url   "https://github.com/user/repository/issues/1"
                                 :labels     [{:name "bug"}
                                              {:name "good first issue"}]
                                 :comments   5
                                 :state      "open"
                                 :title      "Issue test 1"
                                 :user       {:login "usuario"}
                                 :created_at "2024-09-25T13:20:59Z"}]}))))

(deftest test-group-by-author
  (testing "group-by-author deve retornar um mapa com as issues agrupadas por autor"
    (is (= (group-by-author all-issues)
           {"usuario1" [{:html_url   "https://github.com/user/repository/issues/4"
                         :labels     []
                         :comments   0
                         :state      "closed"
                         :title      "Issue test 4"
                         :user       {:login "usuario1"}
                         :created_at "2024-09-27T18:18:55Z"}]
            "usuario"  [{:html_url   "https://github.com/user/repository/issues/3"
                         :labels     []
                         :comments   2
                         :state      "open"
                         :title      "Issue test 3"
                         :user       {:login "usuario"}
                         :created_at "2024-09-25T13:21:31Z"}
                        {:html_url   "https://github.com/user/repository/issues/2"
                         :labels     [{:name "bug"}]
                         :comments   10
                         :state      "open"
                         :title      "Issue test 2"
                         :user       {:login "usuario"}
                         :created_at "2024-09-25T13:21:18Z"}
                        {:html_url   "https://github.com/user/repository/issues/1"
                         :labels     [{:name "bug"}
                                      {:name "good first issue"}]
                         :comments   5
                         :state      "open"
                         :title      "Issue test 1"
                         :user       {:login "usuario"}
                         :created_at "2024-09-25T13:20:59Z"}]}))))

(deftest test-order-by-criteria
  (testing "order-by-comments deve retornar uma lista de issues ordenadas pela quantidade de comentários"
    (is (= (order-by-comments all-issues)
           [{:html_url   "https://github.com/user/repository/issues/4"
             :labels     []
             :comments   0
             :state      "closed"
             :title      "Issue test 4"
             :user       {:login "usuario1"}
             :created_at "2024-09-27T18:18:55Z"}
            {:html_url   "https://github.com/user/repository/issues/3"
             :labels     []
             :comments   2
             :state      "open"
             :title      "Issue test 3"
             :user       {:login "usuario"}
             :created_at "2024-09-25T13:21:31Z"}
            {:html_url   "https://github.com/user/repository/issues/1"
             :labels     [{:name "bug"}
                          {:name "good first issue"}]
             :comments   5
             :state      "open"
             :title      "Issue test 1"
             :user       {:login "usuario"}
             :created_at "2024-09-25T13:20:59Z"}
            {:html_url   "https://github.com/user/repository/issues/2"
             :labels     [{:name "bug"}]
             :comments   10
             :state      "open"
             :title      "Issue test 2"
             :user       {:login "usuario"}
             :created_at "2024-09-25T13:21:18Z"}])))

  (testing "order-by-created_at deve retornar uma lista de issues ordenadas da mais antiga para a mais recente"
    (is (= (order-by-created_at all-issues)
           [{:html_url   "https://github.com/user/repository/issues/1"
             :labels     [{:name "bug"}
                          {:name "good first issue"}]
             :comments   5
             :state      "open"
             :title      "Issue test 1"
             :user       {:login "usuario"}
             :created_at "2024-09-25T13:20:59Z"}
            {:html_url   "https://github.com/user/repository/issues/2"
             :labels     [{:name "bug"}]
             :comments   10
             :state      "open"
             :title      "Issue test 2"
             :user       {:login "usuario"}
             :created_at "2024-09-25T13:21:18Z"}
            {:html_url   "https://github.com/user/repository/issues/3"
             :labels     []
             :comments   2
             :state      "open"
             :title      "Issue test 3"
             :user       {:login "usuario"}
             :created_at "2024-09-25T13:21:31Z"}
            {:html_url   "https://github.com/user/repository/issues/4"
             :labels     []
             :comments   0
             :state      "closed"
             :title      "Issue test 4"
             :user       {:login "usuario1"}
             :created_at "2024-09-27T18:18:55Z"}]))))

(deftest test-count-issues-by-state
  (testing "count-issues-by-state deve retornar o número de issues de acordo com o estado"
    (is (= (count-issues-by-state all-issues "open")
           3))
    (is (= (count-issues-by-state all-issues "closed")
           1))))

(deftest test-count-comments-by-issue
  (testing "count-comments-by-issue deve retornar um mapa com a quantidade de comentários por issue"
    (is (= (count-comments-by-issue all-issues)
           {"Issue test 1" 5
            "Issue test 2" 10
            "Issue test 3" 2
            "Issue test 4" 0}))))
