---
title: 'Web Framework Plugins'
---

`Warden` is built to be portable with your business logic and not tightly coupled to any particular web framework. But,
there are concerns for Authorization that are handled very well at a http layer.

If you are serving content and functionality over http, you want to be sure that you have not accidentally left any
unauthorized doors open.

This is what the Warden web framework plugins aim to achieve; they make sure all the routes go through authorization
unless you specifically open them up for unauthorized access. No actual authorization rules or policies in the http
layer.

## Ktor

As soon as we install the `Warden` ktor feature, every request for any route will be Unauthorized and respond with
a `403` http status code unless the request has been authorized by a `
EnforcementPointKtor`.

### Get Started

Let us look at the minimal setup to get this working.

1) First add the kor plugin dependency along with the core dependency. (Assuming gradle kts).

```kotlin
dependencies {
    //ABAC
    implementation("codes.laurence.warden:warden-core:0.1.0")
    implementation("codes.laurence.warden:warden-ktor:0.1.0")
}
```

2) Set up an `EnforcmentPointKtor`. Here we use the `DecisionPoint` constructor, but you can wrap an
   existing `EnforcementPoint`, or pass in policies directly. This is a drop in replacement for
   an `EnforcementPointDefault`, and it should be wired in to your business logic instead.

```kotlin
var enforcementPoint = EnforcementPointKtor(
    decisionPoint = decisionPointLocal
)
```

2) Install the feature, and wrap all of your routing in a `warded` block.

```kotlin
fun Application.myWebApplication() {

    install(Warden)

    // Setup exception handling to handle any NotAuthorized exceptions and return a `403`
    install(StatusPages) {
        exception<NotAuthorizedException> { call, cause ->
            call.respondText(
                "Not Authorized",
                status = HttpStatusCode.Forbidden
            )
        }
    }

    routing {
        warded {
            /* ----------
             The rest of your routing is wrapped by this block.
             
             As long as you call the `EnforcementPointKtor` on every route, and it allows access, the route will proceed normally.
             ------------*/
        }
    }
}

```

### Open non-enforced routes

It is often the case that you have routes that do not need to be authorized and should be open. 
There are 2 ways to achieve this.

The easiest is to use an `unwarded` routing block, as demonstrated below.

```kotlin
routing {
    warded {
        route("/api") {
            route("/private") {
                // Routes in this block must call an `EnforcementPointKtor`
            }
            route("public") {
                unwarded {
                    // This nested unwarded block is open, even though it has a parent warded block
                }
            }

        }
    }
    unwarded {
        // Routes in this block are open and do not have to be authorized
        route("public") {

        }
    }
}
```

You can also use configuration in the plugin install block to match routes based on regex strings and methods.

The first `WardenRoute` to match the route being called is what will determine the expected behaviour.

```kotlin

install(Warden) {
    routePriorityStack = listOf(
        // This top priority route will be unwarded for Post method calls
        WardenRoute("/api/public/.*", WardenRouteBehaviour.IGNORE, setOf(HttpMethod.Post)),
        // This regex route will be enforced for all http methods, but will be superseded by routes above it.
        WardenRoute("/api/.*", WardenRouteBehaviour.ENFORCE),
    )
}

```

### Websockets

The authorization enforcement on routing to a websocket endpoint can be done in the same way as above. However, the
exceptions and status codes produced by how a websocket connection is established with ktor can be unclear.

To solve this issue, we have a `beforeEach` routing block. See below for an example.

```kotlin
val myChannelKey = AttributeKey<ReceiveChannel<String>>("MessageChannel")
routing {
    route("/ws") {
        warded {
            beforeEach({
                // We get our data and perform authorization before we establish the websocket
                val subscriptionChannel: ReceiveChannel<String> = subscribeToMessages()
                // We can add any necessary variables to the call for later usage in the websocket
                this.call.attributes.put(myChannelKey, subscriptionChannel)
            }) {
                webSocket {
                    // We can retrieve any variables we cached from the `beforeEach` block
                    // and use them in our websocket
                    val subscription = call.attributes[myChannelKey]
                    for (message in subscription) {
                        outgoing.send(Frame.Text(message))
                    }
                }
            }
        }
    }
}
```
