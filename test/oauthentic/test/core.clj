(ns oauthentic.test.core
  (:use [oauthentic.core])
  (:use [clojure.test]))


(deftest using-assoc-params-url 
  (is (= (assoc-query-params "https://test.com/hello?abc=1%20a&one=1"  { :test "this one"})
          "https://test.com/hello?abc=1%20a&one=1&test=this+one"))
  (is (= (assoc-query-params "https://test.com/hello"  { :test "this one" "hello" 123})
          "https://test.com/hello?test=this+one&hello=123"))
  )

(deftest create-authorization-url 
  (is (= (build-authorization-url "https://test.com/hello?abc=1%20a&one=1"  { :client-id "abcdefg" :redirect-uri "http://test.com/link-back" })
          "https://test.com/hello?abc=1%20a&one=1&redirect_uri=http%253A%252F%252Ftest.com%252Flink-back&response_type=code&client_id=abcdefg"))
  (is (= (build-authorization-url "https://test.com/hello?abc=1%20a&one=1"  
        { :client-id "abcdefg" :response-type :token :redirect-uri "http://yoursite.com/oauth/endpoint" })
          "https://test.com/hello?abc=1%20a&one=1&redirect_uri=http%253A%252F%252Fyoursite.com%252Foauth%252Fendpoint&response_type=token&client_id=abcdefg"))
  )
