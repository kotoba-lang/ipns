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

;; Cross-checked against a REAL go-ipfs/Kubo node (0.41.0), not just this
;; library's own prior art: an isolated `ipfs init` (temp IPFS_PATH)
;; generated this keypair; `ipfs id` reported the legacy peer-id and
;; `ipfs cid format -v 1 -b base36 --codec libp2p-key` (converting that
;; peer-id) produced the k51... name below. The PublicKey field
;; (`CAESIJIuTRP5abDMjed8E+iVgoAnxNd5c3rK505fNhZOxHnt`, base64 protobuf)
;; decodes to header `08 01 12 20` + the 32-byte pubkey used here.
(deftest matches-a-real-kubo-node
  (let [pub [0x92 0x2e 0x4d 0x13 0xf9 0x69 0xb0 0xcc 0x8d 0xe7 0x7c 0x13
             0xe8 0x95 0x82 0x80 0x27 0xc4 0xd7 0x79 0x73 0x7a 0xca 0xe7
             0x4e 0x5f 0x36 0x16 0x4e 0xc4 0x79 0xed]]
    (is (= "k51qzi5uqu5djtr2a6bj7pjlv4gdl8zvv8eov1rhknh0vn9uj7ojrozknot04t"
           (ipns/pubkey->name pub))
        "byte-identical to Kubo's own CIDv1 base36 libp2p-key IPNS name for the same key")))

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
