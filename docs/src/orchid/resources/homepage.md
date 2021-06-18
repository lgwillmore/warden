---
title: 'Home'
---
>**Light weight Attribute Based Access Control for Kotlin**

![Build Status](https://github.com/lgwillmore/warden/actions/workflows/test.yml/badge.svg?branch=main)

Attribute Based Access Control (ABAC) is an approach to defining and enforcing authorization rules.

You can find out more about the concepts involved here: [ABAC Overview]({{ link('ABAC Overview') }}).

## The Case for ABAC
 - **Decouple Authorization logic from Routing:** Defining the rules for authorization separately from your routing provides all the benefits of low coupling. In your routing, all you have to ensure is that Authorization is checked, not how or what the Authorization rules are. Your authorization rules are business rules and are formulated against business domain objects, not URLs.
 - **Powerful and expressive Authorization logic:** Role based authorization is a subset of the rules that can be defined with ABAC. You will likely find your needs extending beyond Role Based Authorization quickly and ABAC has you covered.
 - **Architectural components provide flexibility:** With ABAC, and the separation between decision and enforcement, your authorization rules can be leveraged across systems, languages, frontend, backend. It also exposes Policies as business data for CRUD.
 
## Feature Overview
 - Simple but powerful DSL for defining access Policies.
 - An In Memory Decision Point for evaluating access requests given a set of Policies.
 - A General Enforcement Point for guarding execution paths.
 - Framework specific Enforcement Points:
   - Ktor
   
## Planned Features
 - Enforcement Point for Spring
 - Information Point - currently it is up to the developer to populate the full set of attributes before submitting for authorization. More on this later.
 - Tools for CRUDing persistent policies