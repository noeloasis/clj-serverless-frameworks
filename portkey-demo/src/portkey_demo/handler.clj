(ns portkey-demo.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.util.http-response :refer :all]
            [ring.middleware.json :refer [wrap-json-response]]))

(defroutes app-routes
  (GET "/hello" [name]
    (ok {:message (format "Hello World, %s" name)}))

  (GET "/error" []
    (bad-request {:message "Test error"}))

  (route/not-found
   (not-found {:message "Not Found"})))

(def app
  (-> app-routes
      (wrap-json-response)
      (wrap-defaults api-defaults)))
