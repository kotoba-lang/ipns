(ns ipns.core-test
  (:require [clojure.test :refer [deftest is]]
            [ipns.core :as ipns]))

;; Regression vectors cross-checked byte-for-byte against the prior
;; BigInteger-based `ipns-name` in kotoba-lang/kekkai's cacao.clj and
;; kotoba-lang/kagi's identity.clj (ADR-2607050100).
(deftest known-vectors
  (is (= "k51qzi5uqu5dg6lcd99r9gmb963kgugjinxxggwy7o93oagk3f2eg3qcjh7127"
         (ipns/pubkey->name (vec (range 32)))))
  (is (= "k51qzi5uqu5dg6l7sg2ssb5uefnq8g7g1d6n6j2zsio0o0k7snyb11p8myhxxc"
         (ipns/pubkey->name (vec (repeat 32 0)))))
  (is (= "k51qzi5uqu5dmkadisduutrwhobhcd351viuwscvsltkaymqovgho5slq61rlr"
         (ipns/pubkey->name (vec (repeat 32 0xff))))))

(deftest roundtrip
  (doseq [pub [(vec (range 32)) (vec (repeat 32 0)) (vec (repeat 32 0xff))
               (vec (reverse (range 32)))]]
    (is (= pub (ipns/name->pubkey (ipns/pubkey->name pub))))))

(deftest byte-array-input
  (is (= (ipns/pubkey->name (vec (range 32)))
         (ipns/pubkey->name (byte-array (range 32))))))

(deftest validation
  (is (thrown? #?(:clj clojure.lang.ExceptionInfo :cljs js/Error)
               (ipns/pubkey->name (vec (range 31)))))
  (is (thrown? #?(:clj clojure.lang.ExceptionInfo :cljs js/Error)
               (ipns/name->pubkey "not-a-k-name")))
  (is (thrown? #?(:clj clojure.lang.ExceptionInfo :cljs js/Error)
               (ipns/name->pubkey (str "k" (ipns/base36 [0x01 0x00]))))))
