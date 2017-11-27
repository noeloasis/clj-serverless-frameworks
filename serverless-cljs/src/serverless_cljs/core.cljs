(ns serverless-cljs.core
  (:require [cljs-lambda.macros :refer-macros [defgateway]]))

(defn count-input [s]
  (count s))

(defgateway echo [event ctx]
  {:status  200
   :headers {:content-type (-> event :headers :content-type)}
   :body    (let [body (:body event)] 
              (str "body:" body " count:" (count-input body)))})
