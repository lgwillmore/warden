package codes.laurence.warden.docs

import codes.laurence.warden.atts.AttType
import codes.laurence.warden.atts.Atts
import codes.laurence.warden.atts.HasAtts
import codes.laurence.warden.atts.HasAttsI
import codes.laurence.warden.atts.HasAttsMapWrapper
import codes.laurence.warden.atts.attsOf
import codes.laurence.warden.atts.withAtts

/** Shared by every class that represents an account, so policies can match on `type`. */
val AccountType = AttType(type = "Account", typeKeyword = "type")

/** Extending `HasAtts` is all a class needs. Its public properties become its attributes. */
data class Account(val id: String, val email: String, val verified: Boolean) : HasAtts(AccountType)

abstract class Form {
  abstract fun validate(): List<String>
}

/** A class that already has a superclass implements `HasAttsI` with `attsOf` instead. */
data class Signup(val email: String, val handle: String) : Form(), HasAttsI {

  override fun validate(): List<String> = if ("@" in email) emptyList() else listOf("Invalid email")

  override fun atts(): Atts = AccountType.withAtts(attsOf(this))
}

/** Nested `HasAttsI` values, in properties, collections and maps, are converted too. */
data class Team(val name: String, val owner: Account, val members: List<Account>) : HasAtts()

/** Merge extra attributes into an object's attributes without changing the object. */
fun accountInSession(account: Account, sessionId: String): Atts =
    account.withAtts("sessionId" to sessionId)

/** Attributes for an account you only know the ID of. */
fun accountReference(id: String): HasAttsI = AccountType.asHasAtts(mapOf("id" to id))

/** Attributes that arrived as a map, for example from a token or a message. */
fun accountFromClaims(claims: Map<String, Any?>): HasAttsI = HasAttsMapWrapper(claims)
