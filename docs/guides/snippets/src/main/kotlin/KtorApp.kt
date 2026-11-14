package codes.laurence.warden.docs

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.enforce.NotAuthorizedException
import codes.laurence.warden.ktor.EnforcementPointKtor
import codes.laurence.warden.ktor.Warden
import codes.laurence.warden.ktor.WardenRoute
import codes.laurence.warden.ktor.WardenRouteBehaviour
import codes.laurence.warden.ktor.beforeEach
import codes.laurence.warden.ktor.unwarded
import codes.laurence.warden.ktor.warded
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame

class NotAuthenticatedException : Exception("Not Authenticated")

/** Authentication is not Warden's concern. This sample trusts a header. */
fun ApplicationCall.currentUser(users: Map<String, User>): User =
    users[request.header("X-User")] ?: throw NotAuthenticatedException()

fun Application.blogModule(users: Map<String, User>, articles: ArticleStore) {
  val enforcementPoint = EnforcementPointKtor(articlePolicies)
  val service = ArticleService(articles, enforcementPoint)

  install(WebSockets)
  install(Warden) {
    routePriorityStack =
        listOf(
            // The first matching entry wins. Health checks are open for GET only.
            WardenRoute("/health", WardenRouteBehaviour.IGNORE, setOf(HttpMethod.Get)),
            // Everything else is enforced, which is also the default when nothing matches.
            WardenRoute("/.*", WardenRouteBehaviour.ENFORCE),
        )
  }
  install(StatusPages) {
    exception<NotAuthorizedException> { call, _ ->
      call.respondText("Not Authorized", status = HttpStatusCode.Forbidden)
    }
    exception<NotAuthenticatedException> { call, _ ->
      call.respondText("Not Authenticated", status = HttpStatusCode.Unauthorized)
    }
    exception<NoSuchElementException> { call, _ -> call.respond(HttpStatusCode.NotFound) }
  }

  routing {
    // Open through the plugin configuration above.
    get("/health") { call.respondText("ok") }

    // Open through an inline block.
    unwarded { get("/about") { call.respondText("A blog guarded by Warden") } }

    warded {
      route("/articles") {
        get {
          val user = call.currentUser(users)
          val titles = service.listArticles(user).map { it.title }
          call.respondText(titles.joinToString("\n"))
        }
        get("/{id}") {
          val user = call.currentUser(users)
          val article = service.readArticle(user, call.parameters["id"]!!)
          call.respondText(article.title)
        }
        delete("/{id}") {
          val user = call.currentUser(users)
          service.deleteArticle(user, call.parameters["id"]!!)
          call.respond(HttpStatusCode.NoContent)
        }
      }

      // Nothing here calls the enforcement point, so Warden answers 403 instead of the text.
      get("/drafts") { call.respondText("Unpublished drafts") }

      // A websocket cannot be refused once it is open, so authorization runs before the upgrade.
      route("/feed") {
        beforeEach({
          val user = call.currentUser(users)
          enforcementPoint.enforceAuthorization(
              AccessRequest(
                  subject = user.atts(),
                  action = ArticleAction.READ.atts(),
                  resource = ArticleType.atts(),
              ))
        }) {
          webSocket {
            for (article in articles.all()) {
              outgoing.send(Frame.Text(article.title))
            }
          }
        }
      }
    }
  }
}
