(ns oauthentic.test.helpers
  (:use [clj-http.fake]
        [ring.middleware.params]
        [ring.middleware.keyword-params]))


(defn wrap-ring-handler-for-testing
  [handler]
  (let [wrapped (wrap-params (wrap-keyword-params handler))]
    (fn [req] (wrapped (assoc req
                            :body (.getContent (:body req)))))))

(defn dumprequest [req]
  (do
    (prn req)
    {:status 200 :headers {} :body "{\"tx_id\":\"ABCDEF\"}"}
    ))
