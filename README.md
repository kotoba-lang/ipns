# ipns

`kotoba-lang/ipns` is a small EDN-first library for **libp2p-key IPNS names**
— the `k51…` name (a CIDv1, identity-multihash, of the libp2p `PublicKey`
protobuf) deterministically derived from an Ed25519 public key.

An actor's graph IS its key: holding the Ed25519 private key is authority
over the graph named by `pubkey->name` — no registrar, no owner hand-off, no
shared token. This is the naming half of the same discipline `kotoba-lang/did`
covers for `did:key` — the two are siblings derived from the same raw pubkey,
kept as separate libraries because they are different specs (W3C DID vs.
libp2p/IPFS naming), not different concepts of identity.

## Usage

```clojure
(require '[ipns.core :as ipns])

(ipns/pubkey->name (vec (range 32)))
;; => "k51qzi5uqu5dg6lcd99r9gmb963kgugjinxxggwy7o93oagk3f2eg3qcjh7127"

(ipns/name->pubkey "k51qzi5uqu5dg6lcd99r9gmb963kgugjinxxggwy7o93oagk3f2eg3qcjh7127")
;; => [0 1 2 3 ... 31]
```

`pubkey->name` accepts either a JVM `byte[]` or a plain Clojure int vector —
callers on the JVM (e.g. an actor holding a `java.security.PublicKey`) pass
the raw 32-byte public key directly.

## Scope

- `ipns.core` (zero-dep, portable `.cljc`):
  - `pubkey->name` — Ed25519 raw public key → CIDv1 libp2p-key IPNS name (`k51…`)
  - `name->pubkey` — inverse decode, with structural validation
  - `base36` / `base36-decode` — the underlying multibase codec, portable
    `.cljc` (no `BigInteger`, so it runs identically on ClojureScript)
  - Verified byte-identical to a real go-ipfs/Kubo node's IPNS name for
    the same key (`matches-a-real-kubo-node` test — an actual `ipfs
    init`/`ipfs id`/`ipfs cid format` run, not just cross-checked against
    this repo's own prior art).
- `ipns.head` (`:clj`-only — pulls in `ed25519`/`dag-cbor`, `ipns.core`
  itself stays free of them): `sign`/`verify` a mutable IPNS **head
  record** (`{:name :value :sequence :valid_until ...}`, the kotoba
  lexicon `head.json`/`publish.json` shape) over a canonical dag-cbor
  payload, via `ed25519`'s did:key primitives (ADR-2607061800).

Not in scope: publishing/resolving IPNS records over the real IPFS/libp2p
network (host-port concern, see `kotoba-lang/ipfs`; kotobase.net resolves
`ipns.head` records through its own XRPC registry instead, ADR-2607061800),
any DID method (see `kotoba-lang/did`), or a `:cljs` port of `ipns.head`'s
signing (tracked follow-up — `kotobase-client` already has a `@noble/curves`
precedent in its own `cacao.cljc` to port from).

## Provenance

This replaces a byte-for-byte-identical derivation that had been copy-pasted
into two JVM actor-identity call sites — `kotoba-lang/kekkai`'s `cacao.clj`
(`ipns-name`) and `kotoba-lang/kagi`'s `identity.clj` (`ipns-name`) — each a
private `BigInteger`-based reimplementation of the same libp2p-key CIDv1
construction. Both are cross-checked against this library's output
byte-for-byte (see ADR-2607050100).
