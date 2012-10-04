(defproject oauthentic "1.0.0"
  :description "Simple OAuth2 client library"
  :dependencies  [[org.clojure/clojure "1.4.0"]
                  [clj-http "0.5.5"]
                  [ring/ring-core "1.1.0"]]
  :profiles {
    :dev {
      :dependencies [[clj-http-fake "0.4.1"]
                     [clauth "1.0.0-rc10"]]}})