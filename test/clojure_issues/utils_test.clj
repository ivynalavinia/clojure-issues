(ns clojure-issues.utils-test
  (:require [clojure.test :refer :all]
            [clojure-issues.utils :refer :all]))

(deftest test-myDistinct
  (testing "remove duplicados"
    (is (= (myDistinct [1 2 2 3 3 4 4 5]) [1 2 3 4 5])))
  (testing "mantém coleção vazia"
    (is (= (myDistinct []) [])))
  (testing "remove duplicados e não ordena"
    (is (= (myDistinct [3 1 2 3 2 1]) [3 1 2]))))

(deftest test-groupBy
  (testing "agrupa por atributo"
    (is (= (groupBy [{:type :a} {:type :b} {:type :a} {:type :b}] :type)
           {:a [{:type :a} {:type :a}]
            :b [{:type :b} {:type :b}]})))

  (testing "mantém coleção vazia"
    (is (= (groupBy [] :type) {})))

  (testing "se o atributo não existe, retorna coleção vazia"
    (is (= (groupBy [{:type :a} {:type :b}] :non-existent)
           {}))))

(deftest test-orderBy
  (testing "ordena por valor de atributo"
    (is (= (orderBy [{:age 30} {:age 20} {:age 40}] :age)
           [{:age 20} {:age 30} {:age 40}])))
  (testing "mantém coleção vazia"
    (is (= (orderBy [] :age) [])))
  (testing "mantém valores iguais"
    (is (= (orderBy [{:age 30} {:age 30} {:age 30}] :age)
           [{:age 30} {:age 30} {:age 30}]))))

(deftest test-fold
  (testing "aplica a função à coleção"
    (is (= (fold + 0 [1 2 3 4]) 10)))
  (testing "em coleção vazia, retorna valor inicial"
    (is (= (fold + 0 []) 0)))
  (testing "aplica a função à coleção de strings"
    (is (= (fold str "" ["a" "b" "c"]) "abc"))))

(deftest test-compose
  (testing "corretamente aplica composição de funções"
    (is (= ((compose inc #(* % 2)) 3) 7))))
