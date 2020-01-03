---
title: 'Warden'
---
**Light weight Attribute Based Access Control for Kotlin**

![Build Status](https://travis-ci.org/lgwillmore/warden.svg?branch=master)

Attribute Based Access Control (ABAC) is an approach to defining and enforcing authorization rules.

You can find a brief introduction to the concepts involved in ABAC [here]({{ link('ABAC Overview') }}), and will probably help as you learn more.

## Aims of this project
 - Be lightweight and developer friendly
 - Be extendable
 - Be cross platform
 - Be framework agnostic, but provide framework specific plugins.
 
## Feature Overview
 - Simple but powerful DSL for defining access Policies.
 - An In Memory Decision Point for evaluating access requests given a set of Policies.
 - A General Enforcement Point for guarding execution paths.
 - Framework specific Enforcement Points:
   - Ktor
   
## Planned Features
 - Enforcement Point for Spring
 - Information Point - currently it is up to the developer to populate the full set of attributes before submitting for authorization. More on this later.
 - Tools for working with persistent policies (DB backed)