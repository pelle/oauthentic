(ns oauthentic.test.core
  (:use [oauthentic.core]
        [clojure.test]
        [clj-http.fake]
        [ring.middleware.params]
        [ring.middleware.keyword-params])
  (:require
        [clauth.token :as t]
        [clauth.auth-code :as cd]
        [clauth.client :as cl]
        [clauth.endpoints :as e]))


(deftest using-assoc-params-url
  (is (= (assoc-query-params "https://test.com/hello?abc=1%20a&one=1"  { :test "this one"})
          "https://test.com/hello?abc=1%20a&one=1&test=this+one"))
  (is (= (assoc-query-params "https://test.com/hello"  { :test "this one" "hello" 123})
          "https://test.com/hello?test=this+one&hello=123"))
  )

(deftest building-authorization-url
  (is (= "https://test.com/authorize?abc=1%20a&one=1&scope=calendar&redirect_uri=http%3A%2F%2Ftest.com%2Flink-back%3Fproduct_id%3D123%26description%3DSuper%2520Product&response_type=code&client_id=abcdefg"
         (build-authorization-url { :authorization-url "https://test.com/authorize?abc=1%20a&one=1" :client-id "abcdefg"} { :redirect-uri "http://test.com/link-back?product_id=123&description=Super%20Product" :scope "calendar" })))

  (is (= "https://test.com/authorize?abc=1%20a&one=1&redirect_uri=http%3A%2F%2Ftest.com%2Flink-back&response_type=code&client_id=abcdefg"
          (build-authorization-url "https://test.com/authorize?abc=1%20a&one=1"  { :client-id "abcdefg" :redirect-uri "http://test.com/link-back" })))

  (is (= "https://test.com/authorize?abc=1%20a&one=1&redirect_uri=http%3A%2F%2Fyoursite.com%2Foauth%2Fendpoint&response_type=token&client_id=abcdefg"
          (build-authorization-url "https://test.com/authorize?abc=1%20a&one=1"
          { :client-id "abcdefg" :response-type :token :redirect-uri "http://yoursite.com/oauth/endpoint" })))

  (is (=  "https://test.com/authorize?abc=1%20a&one=1&redirect_uri=http%3A%2F%2Fyoursite.com%2Foauth%2Fendpoint&response_type=token&client_id=abcdefg&extra=pelle%40picomoney.com"
          (build-authorization-url "https://test.com/authorize?abc=1%20a&one=1"
            { :client-id "abcdefg" :response-type :token :redirect-uri "http://yoursite.com/oauth/endpoint" :extra "pelle@picomoney.com"})))

  (is (= "https://test.com/authorize?abc=1%20a&one=1&state=ABCD+EF&redirect_uri=http%3A%2F%2Fyoursite.com%2Foauth%2Fendpoint&response_type=token&client_id=abcdefg&extra=pelle%40picomoney.com"
          (build-authorization-url "https://test.com/authorize?abc=1%20a&one=1"
            { :state "ABCD EF" :client-id "abcdefg" :response-type :token :redirect-uri "http://yoursite.com/oauth/endpoint" :extra "pelle@picomoney.com"}))))


(deftest token-requests
  (is (=
    {:accept :json, :as :json, :form-params { :grant_type "client_credentials", :scope "basic"}, :basic-auth ["CLIENT-ID" "SECRET"]}
    (token-request { :client-id "CLIENT-ID" :client-secret "SECRET" :scope "basic" })))
  (is (=
    {:accept :json, :as :json, :form-params { :grant_type "authorization_code", :scope "basic" :redirect_uri "http://test.com/callback" :code "CODE"}, :basic-auth ["CLIENT-ID" "SECRET"] }
    (token-request { :code "CODE" :client-id "CLIENT-ID" :client-secret "SECRET" :scope "basic" :redirect-uri "http://test.com/callback"})))
  )


(defn wrap-ring-handler-for-testing
  [handler]
  (let [wrapped (wrap-params (wrap-keyword-params handler))]
    (fn [req] (wrapped (assoc req
                            :body (.getContent (:body req))
                            :headers (assoc (:headers req) "authorization" ((:headers req) "Authorization")))))
    ))

(defn dumprequest [req]
  (do
    (prn req)
    {:status 200 :headers {} :body "{\"tx_id\":\"ABCDEF\"}"}
    ))

(deftest fetch-oauth-tokens-for-auth-code
  (t/reset-token-store!)
  (cl/reset-client-store!)
  (cd/reset-auth-code-store!)

  (let [ handler (wrap-ring-handler-for-testing (e/token-handler))
         client (cl/register-client) ]

    (with-fake-routes
      {"https://test.com/token" handler }
      (let [ code (:code (cd/create-auth-code client "user" "http://test.com/endpoint"))]
        (is (= (try (fetch-token "https://test.com/token" { :client-id (:client-id client)
                                                            :client-secret (:client-secret client)
                                                            :code code
                                                            :redirect-uri "http://test.com/endpoint" })
                (catch Exception e (prn e)))
            {:access-token ( :token (first (t/tokens))) :token-type :bearer})))

      (let [ code (:code (cd/create-auth-code client "user" "http://test.com/endpoint"))]
        (is (= (try (fetch-token  { :token-url "https://test.com/token"
                                  :client-id (:client-id client)
                                  :client-secret (:client-secret client) }
                                { :code code
                                  :redirect-uri "http://test.com/endpoint"})
              (catch Exception e (prn e)))
          {:access-token ( :token (first (t/tokens))) :token-type :bearer})))
      )))

; http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.4
(deftest fetch-oauth-tokens-for-client-owner
  (t/reset-token-store!)
  (cl/reset-client-store!)
  (cd/reset-auth-code-store!)

  (let [ handler (wrap-ring-handler-for-testing (e/token-handler))
         client (cl/register-client)]

    (with-fake-routes
      {"https://test.com/token" handler }
      (is (= (try (fetch-token "https://test.com/token" {  :client-id (:client-id client)
                                                      :client-secret (:client-secret client)})
              (catch Exception e (prn e)))
          {:access-token ( :token (first (t/tokens))) :token-type :bearer}
      )
      ))))