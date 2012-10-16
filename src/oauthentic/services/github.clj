(ns oauthentic.services.github
  (require [oauthentic.core :as oauth]))

;; https://stripe.com/docs/apps/oauth
(defmethod oauth/token-request :github
  [params]
  (-> params
      (dissoc :service)
      (oauth/token-request)
      (update-in [:form-params]  merge { :client_id (:client-id params) :client_secret (:client-secret params)})
      (dissoc :basic-auth)))

(defmethod oauth/build-authorization-url :github [this params]
  (oauth/build-authorization-url "https://github.com/login/oauth/authorize" params))

(defmethod oauth/fetch-token :github [this params]
  (oauth/fetch-token "https://github.com/login/oauth/access_token" (merge params {:service :github})))

