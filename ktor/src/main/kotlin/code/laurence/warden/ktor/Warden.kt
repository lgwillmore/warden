package code.laurence.warden.ktor

import codes.laurence.warden.decision.DecisionPointInMemory
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.response.ApplicationSendPipeline
import io.ktor.util.AttributeKey

class Warden(config: Configuration) {
    private val enforcementPoint = config.enforcementPoint

    class Configuration {
        var enforcementPoint: EnforcementPointKtor = EnforcementPointKtor(DecisionPointInMemory(emptyList()))
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, Warden> {

        internal val CALL_ENFORCED_ATTRIBUTE_KEY = AttributeKey<Boolean>("warden.enforced")
        internal const val NOT_ENFORCED_MESSAGE = "Not Authorized: EnforcementPoint not enforced"

        override val key = AttributeKey<Warden>("Warden")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): Warden {
            val config = Configuration().apply(configure)
            val warden = Warden(config)

            pipeline.sendPipeline.intercept(ApplicationSendPipeline.After) {
                if (!call.attributes.contains(CALL_ENFORCED_ATTRIBUTE_KEY)) {
                    val content = TextContent(
                        NOT_ENFORCED_MESSAGE,
                        contentType = ContentType.Text.Plain,
                        status = HttpStatusCode.Unauthorized
                    )
                    proceedWith(content)
                }
            }
            return warden
        }
    }
}
