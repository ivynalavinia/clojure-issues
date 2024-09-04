(defproject clojure-issues "0.1.0-SNAPSHOT"
  :description "Um projeto em Clojure para manipulação de issues do GitHub usando programação funcional."
  :url "https://github.com/ivynalavinia/clojure-issues"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [clj-http "3.12.3"] ; Biblioteca para realizar requisições HTTP
                 [cheshire "5.10.1"] ; Biblioteca para manipulação de JSON
                 [org.clojure/tools.cli "1.0.206"]] ; Biblioteca para lidar com CLI
  :main ^:skip-aot clojure-issues.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
