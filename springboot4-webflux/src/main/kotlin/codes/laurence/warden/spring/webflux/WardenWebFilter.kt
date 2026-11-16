package codes.laurence.warden.spring.webflux

import codes.laurence.warden.spring.Unwarded
import codes.laurence.warden.spring.WardenCall
import codes.laurence.warden.spring.WardenCall.Companion.NOT_ENFORCED_MESSAGE
import codes.laurence.warden.spring.WardenCoroutineContext
import codes.laurence.warden.spring.WardenRoute
import codes.laurence.warden.spring.WardenRouteBehaviour
import codes.laurence.warden.spring.evaluateRoute
import codes.laurence.warden.spring.wardenCall
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.withContext
import org.reactivestreams.Publisher
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.server.CoWebFilter
import org.springframework.web.server.CoWebFilterChain
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.context.Context

/**
 * A Spring WebFlux filter that ensures every handler behind it is protected by Warden, or
 * explicitly exempt from authorization.
 *
 * If a handler responds successfully and no `EnforcementPointSpring` was invoked for the request,
 * and the request was not left open by [Unwarded], [unwarded] or [routeStack], the response is
 * replaced with a 403. `OPTIONS` requests are never checked, and responses that are not a success
 * pass through.
 *
 * The decision is made when the first byte of the body is written, or when a bodiless response
 * completes, so streaming responses are not buffered.
 *
 * The filter carries the request into the coroutine context of suspending handlers and into the
 * Reactor context of reactive ones, so an `EnforcementPointSpring` reached anywhere below a handler
 * marks the request as enforced. The Spring Boot auto-configuration registers one with an empty
 * route stack when the application does not define its own.
 */
class WardenWebFilter(routePriorityStack: List<WardenRoute> = emptyList()) :
    CoWebFilter(), Ordered {
  val routeStack: List<WardenRoute> = routePriorityStack.toList()

  companion object {
    /** The exchange attribute holding the request's [WardenCall]. */
    val CALL_ATTRIBUTE: String = WardenWebFilter::class.java.name + ".call"
  }

  /** Runs first, so that it judges the response every other filter settled on. */
  override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE

  override suspend fun filter(exchange: ServerWebExchange, chain: CoWebFilterChain) {
    if (exchange.request.method == HttpMethod.OPTIONS) return chain.filter(exchange)
    val call = WardenCall()
    exchange.attributes[CALL_ATTRIBUTE] = call
    val guarded =
        exchange.mutate().response(WardenResponseDecorator(exchange, call, routeStack)).build()
    val reactorContext = currentCoroutineContext()[ReactorContext]?.context ?: Context.empty()
    withContext(
        WardenCoroutineContext(call) +
            ReactorContext(reactorContext.put(WardenCall.REACTOR_CONTEXT_KEY, call))) {
          chain.filter(guarded)
        }
  }
}

/**
 * Lets the current request respond without authorization being enforced. Call it in a suspending
 * handler; it only takes effect behind a [WardenWebFilter].
 */
suspend fun unwarded() {
  wardenCall()?.ignored = true
}

/**
 * Lets this request respond without authorization being enforced. For handlers that have the
 * exchange and do not suspend; it only takes effect behind a [WardenWebFilter].
 */
fun ServerWebExchange.unwarded() {
  (attributes[WardenWebFilter.CALL_ATTRIBUTE] as? WardenCall)?.ignored = true
}

/**
 * Judges the response as it is written and substitutes the 403 when the request slipped through.
 */
private class WardenResponseDecorator(
    private val exchange: ServerWebExchange,
    private val call: WardenCall,
    private val routeStack: List<WardenRoute>,
) : ServerHttpResponseDecorator(exchange.response) {

  override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> =
      delegate.writeWith(
          Flux.from<DataBuffer>(body).switchOnFirst { _, flux ->
            if (refuses()) refuse(flux) else flux
          })

  override fun writeAndFlushWith(body: Publisher<out Publisher<out DataBuffer>>): Mono<Void> =
      delegate.writeAndFlushWith(
          Flux.from<Publisher<out DataBuffer>>(body).switchOnFirst { _, flux ->
            if (refuses()) Flux.just(refuse(flux.concatMap { Flux.from(it) })) else flux
          })

  override fun setComplete(): Mono<Void> =
      if (refuses()) delegate.writeWith(refuse(Flux.empty())) else delegate.setComplete()

  private fun refuses(): Boolean {
    if (delegate.isCommitted || call.enforced || call.ignored) return false
    val request = exchange.request
    if (evaluateRoute(routeStack, request.uri.path, request.method) ==
        WardenRouteBehaviour.IGNORE) {
      return false
    }
    if (handlerUnwarded()) return false
    val status = delegate.statusCode ?: HttpStatus.OK
    return status.is2xxSuccessful || status.value() == HttpStatus.SWITCHING_PROTOCOLS.value()
  }

  private fun handlerUnwarded(): Boolean {
    val handler =
        exchange.getAttribute<Any>(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE) as? HandlerMethod
            ?: return false
    return handler.hasMethodAnnotation(Unwarded::class.java) ||
        AnnotatedElementUtils.hasAnnotation(handler.beanType, Unwarded::class.java)
  }

  /** Drops [original], releasing its buffers, and answers 403 with the fixed message instead. */
  private fun refuse(original: Flux<out DataBuffer>): Flux<DataBuffer> {
    val bytes = NOT_ENFORCED_MESSAGE.toByteArray(StandardCharsets.UTF_8)
    delegate.statusCode = HttpStatus.FORBIDDEN
    delegate.headers.contentType = MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8)
    delegate.headers.contentLength = bytes.size.toLong()
    return original
        .doOnNext { DataBufferUtils.release(it) }
        .ignoreElements()
        .thenMany(Mono.fromSupplier { delegate.bufferFactory().wrap(bytes) })
  }
}
