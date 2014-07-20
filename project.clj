(defproject noo "0.0.1-SNAPSHOT"
  :description "noo is nature-of-om is an attempt to recreate  Nature of Code with Om Components"
  :url "http://github.com/squiddle/nature-of-om"
  :license {:name "GNU Affero GPL v3.0"
            :url "http://www.gnu.org/licenses/agpl-3.0.txt"}

  :jvm-opts ^:replace ["-Xms512m" "-Xmx512m" "-server"]

  :source-paths  ["src"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2261" :scope "provided"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha" :scope "provided"]
                 [om "0.6.4"]
                 [om/art "0.0.1-SNAPSHOT"]
                 [com.facebook/react-art "0.10.2"]
                 [com.facebook/react "0.10.0" :scope "provided"]] ;;part of react-art

  :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]]

  :cljsbuild {
    :builds [
             {:id "app"
              :source-paths ["src"]
              :compiler {
                          :foreign-libs [{:file "react-art/ReactART.js" :provides ["React" , "ReactArt"]}]
                          :output-to "script/main.js"
                          :output-dir "script/out"
                          :source-map true
                          :optimizations :none
                          :pretty-print true}}
             ]})
