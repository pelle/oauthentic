(ns oauthentic.test.services.stripe
  (:use [clojure.test]
        [clj-http.fake])
  (:require
        [oauthentic.services.stripe :as stripe]
        [oauthentic.core :as oauth]
        [cheshire.core :as json]))



(deftest building-authorization-url
  (is (= "https://connect.stripe.com/oauth/authorize?state=ABCDEF&scope=read_write&redirect_uri=http%3A%2F%2Ftest.com%2Fcallback&response_type=code&client_id=CLIENT"
          (oauth/build-authorization-url :stripe { :client-id "CLIENT" :state "ABCDEF" :scope :read_write :redirect-uri "http://test.com/callback"}))))

(deftest token-requests
  (is (=
    {:accept :json, :as :json, :form-params { :grant_type "authorization_code", :scope "basic" :redirect_uri "http://test.com/callback" :code "CODE" :client_id "CLIENT-ID"}, :oauth-token "SECRET" }
    (oauth/token-request { :service :stripe :code "CODE" :client-id "CLIENT-ID" :client-secret "SECRET" :scope "basic" :redirect-uri "http://test.com/callback"})))
  (is (=
    {:accept :json, :as :json, :form-params { :grant_type "refresh_token", :scope "basic" :refresh_token "REFRESH" :client_id "CLIENT-ID"}, :oauth-token "SECRET" }
    (oauth/token-request { :service :stripe :refresh-token "REFRESH" :client-id "CLIENT-ID" :client-secret "SECRET" :scope "basic"}))))

(defn stripe-request [{ :keys [headers] :as req }]
  (if (= "Bearer SECRET" (headers "authorization"))
    {:status 200 :headers {} :body (json/generate-string {  :scope  "read_write" ;(:scope params)
                                                            :livemode true
                                                            :access_token "TOKEN"
                                                            :token_type "bearer"
                                                            :refresh_token "REFRESH"
                                                            :stripe_user_id "USER_ID"
                                                            :stripe_publishable_key "PUBLISHABLE_KEY"})}
    {:status 401 :body ""}))

(deftest fetch-oauth-tokens-for-auth-code
  (with-fake-routes
    { "https://connect.stripe.com/oauth/token" stripe-request }
      (is (=
          { :scope "read_write"
            :livemode true
            :access-token "TOKEN"
            :token-type :bearer
            :refresh-token "REFRESH"
            :stripe_user_id "USER_ID"
            :stripe_publishable_key "PUBLISHABLE_KEY"}
          (oauth/fetch-token :stripe {  :client-id (:client-id "CLIENT")
                              :client-secret "SECRET"
                              :code "CODE"
                              :scope :read_write
                              :redirect-uri "http://test.com/endpoint" })))))
