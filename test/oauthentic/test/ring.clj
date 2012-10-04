(ns oauthentic.test.ring
  (:use [oauthentic.core]
        [oauthentic.ring]
        [clj-http.fake]
        [clojure.test]))


(let [handler (oauthentic-handler (fn [req] (req :oauthentic-token) ) ;; login handler just returns token for testing purpose
                                  (fn [req] (req :oauthentic-error) ) ;; error handler just returns token for testing purpose
                                  { :authorization-url "https://testhub.com/authorize"
                                    :token-url "https://testhub.com/token"
                                    :client-id "abcdefg"
                                    :client-secret "ssh"} )]

  (deftest should-redirect-to-service
    (let [response (handler { :params {}
                              :scheme :https
                              :server-name "testapp.com"
                              :server-port 80
                              :uri "/connect"})]
      (is (= 302 (response :status)))
      (is (= {"Location" "https://testhub.com/authorize?redirect_uri=https%3A%2F%2Ftestapp.com%3A80%2Fconnect&response_type=code&client_id=abcdefg"}
              (response :headers)))
    ))


  (deftest should-fetch-token

    (with-fake-routes
      { "https://testhub.com/token"
          (fn [req] {:status 200 :headers {} :body "{\"access_token\":\"TOKENEFG\",\"token_type\":\"bearer\"}"})}

      (is (= {:access-token "TOKENEFG" :token-type :bearer }
                  (handler {  :params { :code "CODEBCD" }
                              :scheme :https
                              :server-name "testapp.com"
                              :server-port 80
                              :uri "/connect"})))

      ))

  (deftest should-handle-auth-errors

    (is (= "invalid_client" (handler  { :params { :error "invalid_client" }
                                        :scheme :https
                                        :server-name "testapp.com"
                                        :server-port 80
                                        :uri "/connect"})))))

