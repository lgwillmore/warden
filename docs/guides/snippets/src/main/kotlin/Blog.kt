package codes.laurence.warden.docs

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.FilterAccessRequest
import codes.laurence.warden.ResourceAttributePair
import codes.laurence.warden.atts.AttType
import codes.laurence.warden.atts.Atts
import codes.laurence.warden.atts.HasAtts
import codes.laurence.warden.atts.HasAttsI
import codes.laurence.warden.enforce.EnforcementPoint
import codes.laurence.warden.policy.Policy
import codes.laurence.warden.policy.bool.allOf
import codes.laurence.warden.policy.expression.resourceVal

val UserType = AttType(type = "User", typeKeyword = "type")

val ArticleType = AttType(type = "Article", typeKeyword = "type")

/** A signed-in user. `HasAtts` turns its properties into the subject's attributes. */
data class User(val id: String, val roles: List<String>) : HasAtts(UserType)

/** A blog post. Its properties, with the `type` attribute, are the resource's attributes. */
data class Article(val id: String, val title: String, val authorId: String) : HasAtts(ArticleType)

/** What a user is trying to do to an article. */
enum class ArticleAction : HasAttsI {
  READ,
  CREATE,
  MODIFY,
  DELETE;

  override fun atts(): Atts = mapOf("type" to name)
}

val articlePolicies: List<Policy> =
    listOf(
        // Any user can read any article.
        allOf {
          resource("type") equalTo "Article"
          action("type") equalTo "READ"
        },
        // The author of an article can modify and delete it.
        allOf {
          resource("type") equalTo "Article"
          action("type") isIn listOf("MODIFY", "DELETE")
          subject("id") equalTo resourceVal("authorId")
        },
        // Only users with the Author role can create articles.
        allOf {
          resource("type") equalTo "Article"
          action("type") equalTo "CREATE"
          subject("roles") contains "Author"
        },
    )

/** Stands in for a database. */
class ArticleStore(initial: List<Article> = emptyList()) {
  private val articles = initial.associateBy { it.id }.toMutableMap()

  fun all(): List<Article> = articles.values.toList()

  fun get(id: String): Article = articles[id] ?: throw NoSuchElementException("No article $id")

  fun save(article: Article) {
    articles[article.id] = article
  }

  fun remove(id: String) {
    articles.remove(id)
  }
}

/**
 * The service layer. Every operation builds an [AccessRequest] from the user, the action and the
 * article, and lets the enforcement point stop it before anything happens.
 */
class ArticleService(
    private val articles: ArticleStore,
    private val enforcementPoint: EnforcementPoint,
) {

  suspend fun readArticle(user: User, articleId: String): Article {
    val article = articles.get(articleId)
    enforcementPoint.enforceAuthorization(
        AccessRequest(
            subject = user.atts(),
            action = ArticleAction.READ.atts(),
            resource = article.atts(),
        ))
    return article
  }

  suspend fun createArticle(user: User, article: Article): Article {
    enforcementPoint.enforceAuthorization(
        AccessRequest(
            subject = user.atts(),
            action = ArticleAction.CREATE.atts(),
            resource = article.atts(),
        ))
    articles.save(article)
    return article
  }

  suspend fun deleteArticle(user: User, articleId: String) {
    val article = articles.get(articleId)
    enforcementPoint.enforceAuthorization(
        AccessRequest(
            subject = user.atts(),
            action = ArticleAction.DELETE.atts(),
            resource = article.atts(),
        ))
    articles.remove(articleId)
  }

  /** Only the articles this user may read. */
  suspend fun listArticles(user: User): List<Article> =
      enforcementPoint.filterAuthorization(
          FilterAccessRequest(
              subject = user.atts(),
              action = ArticleAction.READ.atts(),
              resources = articles.all().map { ResourceAttributePair(it, it.atts()) },
          ))
}
