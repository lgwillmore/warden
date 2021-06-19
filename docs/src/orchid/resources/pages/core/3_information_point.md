---
title: 3) Information Point
---

An `InformationPoint` is used by the `DecisionPoint` and it lets us enrich partially complete attributes of
an `AccessRequest` with more attributes. Possible scenarios are:

- There may be other entities or resources associated with a given resource, but which we want to use in our policy
  rules. We can fetch and merge those attributes into the existing attributes of the primary resource.
- When you fetch a resource by ID, generally all you know is the ID and the type. A workaround is to only check the
  authorization after you have retrieved the resource, but what if you want to just check permissions?

The `InformationPoint` enrich function gets full access to the request, and so you can implement the retrieval and
enrichment of attributes as you wish.