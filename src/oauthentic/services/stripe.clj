(ns oauthentic.services.stripe
  (require [oauthentic.core :as oauth]))

;; https://stripe.com/docs/apps/oauth
(defmethod oauth/token-request :stripe
  [params]
  (-> params
      (dissoc :service)
      (oauth/token-request)
      (assoc-in [:form-params :client_id] (:client-id params))
      (dissoc :basic-auth)
      (assoc :oauth-token (:client-secret params))))

(defmethod oauth/build-authorization-url :stripe [this params]
  (oauth/build-authorization-url "https://connect.stripe.com/oauth/authorize" params))

(defmethod oauth/fetch-token :stripe [this params]
  (oauth/fetch-token "https://connect.stripe.com/oauth/token" (merge params {:service :stripe})))

