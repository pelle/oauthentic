(ns oauthentic.services.stripe
  (require [oauthentic.core :as oauth]))

;; https://stripe.com/docs/apps/oauth
(defmethod oauth/token-request :stripe
  [params]
  { :accept :json :as :json
    :form-params (-> params
                    (dissoc :redirect-uri :client_id)
                    (assoc :client_id (:client-id params)
                            :redirect_uri (:redirect-uri params))
                    (select-keys [:code :scope :redirect_uri])
                    (assoc :client_id (:client-id params)
                           :grant_type "authorization_code"))
    :oauth-token (:client-secret params)})

(defmethod oauth/build-authorization-url :stripe [this params]
  (oauth/build-authorization-url "https://connect.stripe.com/oauth/authorize" params))

(defmethod oauth/fetch-token :stripe [this params]
  (oauth/fetch-token "https://connect.stripe.com/oauth/token" (merge params {:service :stripe})))

