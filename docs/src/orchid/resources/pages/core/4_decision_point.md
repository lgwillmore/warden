---
title: 4) Decision Point
---

The `DecisionPoint` is where most of the work is done for allowing/denying an `AccessRequest`.

1) The `DecisionPoint` will enrich the attributes of the `AccessRequest` with the `InformationPoint`
2) The `DecisionPoint` will retrieve Policies based on the access request itself from a `PolicySource`.
3) The `DecisionPoint` will evaluate all allow and deny policies for the `AccessRequest` and return a result.

The evaluation works over a set of `allow` policies and a set of `deny` policies. The result of the evaluation will
depend on:

- if there is not an `allow` policy that grants access, there is no access.
- if there is an `allow` policy that grants access, but the is also a `deny` policy that grants access, there is no
  access.
- if there is an `allow` policy that grants access and no `deny` policy that grants access, there is access.


