---
title: 'FAQ'
---

### Why are the access requests and policies not typed against actual domain objects with generics?

We are big fans of strong typing, but in the case of defining the attributes for an access request
and policies themselves:

- Very often, you do not need the full Entity to perform a check, a partial set of attributes will
  be sufficient and efficient.
- Perhaps you want to assemble attributes for any part of the request from multiple sources
- Policies may not care about the type of a given Subject, or Resource etc., merely that some id
  matches for example.
- We did not want to have complex reflexion logic or generics on the inside of the evaluation
  boundary.
- Bags of attributes is what a Subject, Resource, Action, Environment are. Keeping the core logic
  focused on this will let more complex functionality be built in layers above it. Serialization of
  entity types to Maps of attributes can happen as an external layer, and validation of policies
  against known business domain objects can as well. These are things that will likely be built
  later.
