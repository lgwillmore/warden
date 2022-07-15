---
title: 'ABAC Overview'
---

Attribute Based Access Control (ABAC) is an authorization paradigm that uses policies built around
the attributes of an access request.

ABAC is powerful; the more traditional role based authorization approaches can easily be implemented
using ABAC, but so can more complex rules.

There are several concepts that define how ABAC achieves this.

## The Access Request

The access request is made up of 4 parts, each with its own attributes:

- **The Subject**: Most often this will represent the user making a request, and examples of the
  attributes would be 'id', 'roles' etc.
- **The Action**: The action defines some sort of verb that is being attempted. For example, READ,
  WRITE, DELETE. But it can also have other attributes like 'Force'
- **The Resource**: The item that the action is being applied to. This could be any bit of data that
  can be pointed at by a URL.
- **The Environment**: Other contextual information about the request, for example the ip address or
  browser agent.

By having access to all of this information about the request, we can now define rules and logic to
grant access. This is where policies come in.

## Policies

A policy is an encapsulation of a rule or logic that will be able to determine if a given request is
authorized. Generally they consist of sets of boolean logica expressions.

## Decision Point

This is some service running either locally or remotely which will have access to all the needed
policies. As such, it is capable of deciding if a given request is authorized. There can be many
Policies, and generally things operate on a whitelist basis; if a single policy grants access, then
access is granted. If none do, access is not granted

## Enforcement Point

This is a layer or guard around the service that can actually act upon a given request. It will use
a `Decision Point` to determine if the request is authorized, and then either allow execution of the
request or prevent it.

## Information Point

An information bridge that allows for a `Decision Point` to enhance or fetch any missing attributes
for a given access request. For example, the only attribute might be an id, and it is likely that
many policies will need more than that to properly function.
