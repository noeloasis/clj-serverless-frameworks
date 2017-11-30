;; gorilla-repl.fileformat = 1

;; @@
(ns portkey-demo
  (:require [portkey.core :as pk]))
;; @@

;; @@
(defn hello [name] (format "Hi, %s!" name))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;portkey-demo/hello</span>","value":"#'portkey-demo/hello"}
;; <=

;; @@
(pk/mount! hello "/hello?name={name}")
;; @@
