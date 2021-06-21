---
title: 'Home'
---
> **Attribute Based Access Control for Kotlin**
>
> - Simple and expressive Policy based Authorization
> - Decoupled from web frameworks

![Build Status](https://github.com/lgwillmore/warden/actions/workflows/test.yml/badge.svg?branch=main) ![version](https://img.shields.io/github/v/tag/lgwillmore/warden?include_prereleases&label=release)

## What is Attribute Based Access Control?

ABAC allows us to define access/deny policies based around any conceivable attribute of a request. Attributes of:

- **The subject**: The entity performing the request/action, typically a user.
- **The action**: The action being performed eg. A Forced update.
- **The resource**: The entity the action is related to or being performed on.
- **The environment**: Any environmental attributes eg. Time, IP address, location.

With policies defined using rules with access to this information we can then enforce our Policies in any part of a
distrubuted system.

You can find out more about the concepts involved here: [ABAC Overview]({{ link('ABAC Overview') }}).

## Advantages of ABAC

ABAC has the following advantages over role based access control implemented with your flavour of
web framework:

### Decouple Authorization from Routing

Defining the rules for authorization separately from your routing provides all the benefits of low coupling.

In your routing, all you have to ensure is that Authorization is checked, not how or what the Authorization rules are.

Your authorization rules are business rules and are formulated against business domain objects, not URLs. You can have
multiple URLs that need the same rules to be enforced, and this is better done in a lower layer.

### Expressive Authorization logic

Role based authorization is a subset of the rules that can be defined with ABAC. You will likely find your needs
extending beyond Role Based Authorization quickly and ABAC has you covered.

### Architectural flexibility

With ABAC, and the separation between decision and enforcement, your authorization rules can be leveraged across
systems, languages, frontend, backend. It also exposes Policies as business data for CRUD.

## Feature Overview

- Simple but powerful DSL for defining access Policies.
- A local Decision Point for evaluating access requests given a source of Policies.
- An InformationPoint hook for silently enriching request attributes.
- A General Enforcement Point for guarding execution paths.
- Framework specific Enforcement Points:
    - Ktor

## Planned Features

- Enforcement Point for Spring
- Tools for CRUDing persistent policies
- Policy naming
- More informative responses to allow insight into how a request was approved/denied