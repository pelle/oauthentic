(ns oauthentic.ring
  (:use [oauthentic.core]
        [ring.util.response :only [redirect]])
  (:import (java.net URI)))


(defn oauthentic-error-handler
  [req] (redirect "/"))

(defn oauthentic-login-handler
  [req] (assoc (redirect "/") :session {:oauthentic-token (req :oauthentic-token)}))

(defn oauthentic-handler
  "Performs an oauth authorization towards a third party service

   Install one per service you want to authenticate against at different routes.

   login-handler is an ordinary ring handler which is called if authorization is successfull.
   
   It receives an :oauthentic-token in the request map containing the token.

   error-handler is an ordinary error handler which is called if service redirects back with an error.

   It receives an :oauthentic-error in the request map containing the error code.

   service-params are a map of the following required parameters that are provided by the service:

   - :client-id
   - :client-secret
   - :authorization-url
   - :token-url
  "
  ([service-params]
    (oauthentic-handler oauthentic-login-handler oauthentic-error-handler service-params))

  ([login-handler service-params]
    (oauthentic-handler login-handler oauthentic-error-handler service-params))

  ([login-handler error-handler service-params]
    (fn [req]
      (let [ redirect-uri (str (URI.  (name (req :scheme))
                                      nil
                                      (req :server-name)
                                      (req :server-port) ;; Need to handle case with default ports to remove unsightly :443s and :80s
                                      (req :uri)
                                      (req :query-string)
                                      nil))
             params (req :params)]
        (cond
          (params :code)
            (let [ token (fetch-token service-params {:code (params :code) :redirect-uri redirect-uri })]
              (login-handler (assoc req :oauthentic-token token)))
          (params :error)
            (error-handler (assoc req :oauthentic-error (params :error)))
          :else
            (redirect (build-authorization-url service-params {:redirect-uri redirect-uri})))))))