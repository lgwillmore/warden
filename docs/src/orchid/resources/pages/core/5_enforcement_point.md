---
title: 5) Enforcement Point
---

An `EnforcementPoint` converts the result of a `DesicionPoint` evaluation into a thrown `NotAuthorizedException` if
access was denied.

The purpose of the `EnforcementPoint` call is to prevent any further processing of the `AccessRequest`