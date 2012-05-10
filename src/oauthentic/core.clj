(ns oauthentic.core
  (:require [clj-http.client :as client])
  (:import (java.net URI)))

(defn assoc-query-params
  "Add map of parameters to query section of url.
  It does not attempt to remove duplicates in existing query string
  "
  [url params]
  (let [u (URI. url) 
        q (.getQuery u)
        nq (client/generate-query-string params)]
    (str (URI.
      (.getScheme u)
      (.getUserInfo u)
      (.getHost u) 
      (.getPort u)
      (.getPath u)
      (if q
        (str q "&" nq)
        nq )          
      (.getFragment u)))))

(defn build-authorization-url
  "Create a OAuth authorization url for redirection or link"
  ( [authorization-url params]
    (build-authorization-url authorization-url (:client-id params) 
                                            (:response-type params) 
                                            (:redirect-uri params)
                                            (dissoc params :client-id :response-type :redirect-uri)))

  ( [authorization-url client-id response-type redirect-uri params ] 
    (let [ qp  (reduce #( assoc %1 (name (key %2)) (val %2)) {}  
              {"client_id" client-id "response_type" (name (or response-type "code")) "redirect_uri" redirect-uri })]
      (assoc-query-params authorization-url qp))))

(defn fetch-token
  "Fetch an oauth token from server"
  [url params]
  (let [response (:body (client/post url {
              :basic-auth [(:client-id params) (:client-secret params)]
              :accept :json
              :as :json
              :form-params {
                :grant_type "authorization_code"
                :code (:code params)
                :redirect_uri (:redirect-uri params)
                }}))]
    { :access-token (:access_token response) :token-type (keyword (:token_type response))}))