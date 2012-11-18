(ns oauthentic.test.services.github
  (:use [clojure.test]
        [oauthentic.test.helpers]
        [clj-http.fake])
  (:require
        [oauthentic.services.github :as github]
        [oauthentic.core :as oauth]
        [cheshire.core :as json]))



(deftest building-authorization-url
  (is (= "https://github.com/login/oauth/authorize?state=ABCDEF&scope=read_write&redirect_uri=http%3A%2F%2Ftest.com%2Fcallback&response_type=code&client_id=CLIENT"
          (oauth/build-authorization-url :github { :client-id "CLIENT" :state "ABCDEF" :scope :read_write :redirect-uri "http://test.com/callback"}))))

(deftest token-requests
  (is (=
    {:accept :json, :as :json, :form-params { :grant_type "authorization_code", :redirect_uri "http://test.com/callback" :code "CODE" :client_id "CLIENT-ID" :client_secret "SECRET"}, :insecure? false }
    (oauth/token-request { :service :github :code "CODE" :client-id "CLIENT-ID" :client-secret "SECRET" :redirect-uri "http://test.com/callback"}))))

(defn github-request [{ :keys [params] :as req }]
  (if (and (= "SECRET" (:client_secret params)) (= "CLIENT" (:client_id params)))
    {:status 200 :headers {} :body (json/generate-string {  :access_token "TOKEN"
                                                            :token_type "bearer"})}
    {:status 401 :body ""}))

(deftest fetch-oauth-tokens-for-auth-code
  (with-fake-routes
    { "https://github.com/login/oauth/access_token" (wrap-ring-handler-for-testing github-request) }
      (is (=
          { :access-token "TOKEN"
            :token-type :bearer}
          (oauth/fetch-token :github {  :client-id "CLIENT"
                              :client-secret "SECRET"
                              :code "CODE"
                              :redirect-uri "http://test.com/endpoint" })))))
