(ns ipns.head-test
  (:require #?(:clj [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer [deftest is testing] :include-macros true])
            #?(:clj [ipns.head :as head])
            #?(:clj [ed25519.core :as ed])
            [ipns.core :as ipns]))

#?(:clj
   (deftest sign-and-verify-roundtrip
     (let [seed (byte-array (range 32))
           name (ipns/pubkey->name (ed/pubkey-from-seed seed))
           record {:name name :value "bafyreicid..." :sequence 1
                    :valid_until "2027-01-01T00:00:00Z"}
           signed (head/sign seed record)]
       (testing "round-trips through sign then verify"
         (is (= {:valid? true :name name} (head/verify signed))))
       (testing "tampering with a signed field invalidates it"
         (is (= false (:valid? (head/verify (assoc signed :sequence 2))))))
       (testing "a different signer's pubkey fails verification"
         (let [other-seed (byte-array (range 1 33))]
           (is (= false (:valid? (head/verify
                                   (assoc signed :public_key_multibase
                                          (ed/did-key-from-seed other-seed)))))))))))
