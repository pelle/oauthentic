(ns oauthentic.core
  (:require [clj-http.client :as client]
            [clj-http.util :as u])
  (:import (java.net URI)))

(defn assoc-query-params
  "Add map of parameters to query section of url.
  It does not attempt to remove duplicates in existing query string
  "
  [url params]
  (let [u (URI. url)
        q (.getRawQuery u)
        nq (client/generate-query-string params)
        fr (.getRawFragment u)]
    (str (URI.
      (.getScheme u)
      (.getUserInfo u)
      (.getHost u)
      (.getPort u)
      (.getPath u)
      nil nil ) ;; Have to add query and path manually as URI reencodes the query and fragment
      "?"
      (if q
        (str q "&" nq)
        nq )
      (if fr
        (str "#" fr))
      )))

(defn create-authorization-url [authorization-url client-id response-type redirect-uri params ]
    (let [ qp  ( merge params (reduce #( assoc %1 (name (key %2)) (val %2)) {}
                  {"client_id" client-id "response_type" (name (or response-type "code")) "redirect_uri" redirect-uri }))
          ]
      (assoc-query-params authorization-url qp)))


(defmulti build-authorization-url "Create a OAuth authorization url for redirection or link"  (fn [this _] (class this)))

(defmethod build-authorization-url java.util.Map [this params]
  (build-authorization-url (:authorization-url this) (merge (select-keys this [:client-id ]) params)))

(defmethod build-authorization-url :default
  [authorization-url params]
  (create-authorization-url (str authorization-url) (:client-id params)
                                              (:response-type params)
                                              (:redirect-uri params)
                                              (dissoc params :client-id :response-type :redirect-uri)))

(defn grant-type
  "Return the grant-type request"
  [params]
  (cond
    (:service params) (:service params) ;; Use :service to create a token-request for a specific service. EG Stripe has very different ways of handling it.
    (:grant-type params) (keyword (:grant-type params))
    (:code params) :authorization-code
    (:refresh-token params) :refresh-token
    (:username params) :password
    :else :client-credentials))

(defmulti token-request "Create a token request map" grant-type)

;; http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.1.3
(defmethod token-request :authorization-code
  [params]
  { :accept :json :as :json
    :form-params (assoc (select-keys (assoc params :redirect_uri (:redirect-uri params))
                             [:code :scope :redirect_uri])
                        :grant_type "authorization_code")
    :basic-auth [(:client-id params) (:client-secret params)]})

;; http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.4
(defmethod token-request :client-credentials
  [params]
  { :accept :json :as :json
    :form-params (assoc (select-keys params [:scope])
                    :grant_type "client_credentials" )
    :basic-auth [(:client-id params) (:client-secret params)]})

;; http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.3
(defmethod token-request :username
  [params]
  { :accept :json :as :json
    :form-params (assoc (select-keys params [:username :password :scope])
                        :grant_type "password")

    :basic-auth [(:client-id params) (:client-secret params)]})

;; http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-6
(defmethod token-request :refresh-token
  [params]
  { :accept :json :as :json
    :form-params (merge { :refresh_token (:refresh-token params) :grant_type "refresh_token" }
                    (select-keys params [:scope]))
    :basic-auth [(:client-id params) (:client-secret params)]})

;; https://stripe.com/docs/apps/oauth
(defmethod token-request :stripe
  [params]
  { :accept :json :as :json
    :form-params (assoc (select-keys (assoc params :redirect_uri (:redirect-uri params))
                             [:code :scope :redirect_uri :client-id])
                    :grant_type "authorization_code" )
    :token (:client-secret params)})


(defmulti fetch-token "Fetch an oauth token from server" (fn [this _] (class this)))

(defmethod fetch-token java.util.Map [this params]
  (fetch-token (:token-url this ) (merge (select-keys this [:client-id :client-secret]) params)))


(defmethod fetch-token :default
  [url params]
  (let [response (:body (client/post url (token-request params)))]
    ;; TODO return everything returned
    { :access-token (:access_token response) :token-type (keyword (:token_type response))}))


