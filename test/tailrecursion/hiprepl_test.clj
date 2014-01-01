(ns tailrecursion.hiprepl-test
  (:require [clojure.test :refer :all]
            [tailrecursion.hiprepl :refer :all]))

(deftest message-handler-test
  (let [handler (message-handler "I Robot")]
    (testing "evaluates messages starting with ','"
      (is (= "4" (handler {:body ",(+ 2 2)"}))))
    (testing "limits the number of elements printed"
      (is (= "(0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 ...)")))
    (testing "does not evaluate messages that do not start with ','"
      (is (nil? (handler {:body "(+ 2 2)"}))))
    (testing "captures *out*"
      (is (= "Hello\nnil" (handler {:body ",(println \"Hello\")"}))))
    (testing "does not evaluate its own output"
      (is (nil? (handler {:body ",(+ 2 2)",
                          :from "9645_Foo@conf.hipchat.com/I Robot"}))))))
