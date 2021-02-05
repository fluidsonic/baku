package io.fluidsonic.server

import io.fluidsonic.json.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.flow.*
import kotlin.reflect.*


class BakuModuleConfiguration<Context : BakuContext, Transaction : BakuTransaction> internal constructor(
	internal val module: BakuModule<Context, Transaction>
) {

	@PublishedApi
	internal val commands = Commands()

	@PublishedApi
	internal val entities = Entities()

	internal val additionalResponseEncodings: MutableList<JsonEncoder<Transaction>.() -> Unit> = mutableListOf()
	internal val bsonCodecProviders: MutableList<BSONCodecProvider<Context>> = mutableListOf()
	internal val commandRoutes: MutableList<BakuCommandRoute<Transaction>> = mutableListOf()
	internal val customConfigurations: MutableList<Application.() -> Unit> = mutableListOf()
	internal val idFactories: MutableSet<EntityId.Factory<*>> = mutableSetOf()
	internal val jsonCodecProviders: MutableList<JsonCodecProvider<Transaction>> = mutableListOf()
	internal val routeConfigurations: MutableList<Route.() -> Unit> = mutableListOf()
	internal val routeWrappers: MutableList<Route.(next: Route.() -> Unit) -> Route> = mutableListOf()
	internal val routedCommandNames: HashSet<BakuCommandName> = hashSetOf()


	fun bson(vararg providers: BSONCodecProvider<Context>) {
		bsonCodecProviders += providers
	}


	inline fun commands(configure: Commands.() -> Unit) {
		commands.configure()
	}


	fun custom(configure: Application.() -> Unit) {
		customConfigurations += configure
	}


	inline fun entities(configure: Entities.() -> Unit) {
		entities.configure()
	}


	fun ids(vararg factories: EntityId.Factory<*>) {
		idFactories += factories
	}


	fun json(vararg providers: JsonCodecProvider<Transaction>) {
		jsonCodecProviders += providers
	}


	fun additionalResponseEncoding(encode: JsonEncoder<Transaction>.() -> Unit) {
		additionalResponseEncodings += encode
	}


	@ContextDsl
	fun routes(configure: Route.() -> Unit) {
		routeConfigurations += configure
	}


	fun wrapAllRoutes(wrapper: Route.(next: Route.() -> Unit) -> Route) {
		routeWrappers += wrapper
	}


	private fun Route.addRoute(method: HttpMethod, commandFactory: BakuCommandFactory<Transaction, *, *>) {
		method(method) {
			handle(commandFactory = commandFactory)
		}
	}


	private fun Route.addRoute(method: HttpMethod, path: String, commandFactory: BakuCommandFactory<Transaction, *, *>) =
		route(path) {
			addRoute(method = method, commandFactory = commandFactory)
		}


	@ContextDsl
	fun Route.delete(path: String, commandFactory: BakuCommandFactory<Transaction, *, *>) =
		addRoute(method = HttpMethod.Delete, path = path, commandFactory = commandFactory)


	@ContextDsl
	fun Route.delete(commandFactory: BakuCommandFactory<Transaction, *, *>) =
		addRoute(method = HttpMethod.Delete, commandFactory = commandFactory)


	@ContextDsl
	fun Route.get(path: String, commandFactory: BakuCommandFactory<Transaction, *, *>) =
		addRoute(method = HttpMethod.Get, path = path, commandFactory = commandFactory)


	@ContextDsl
	fun Route.get(commandFactory: BakuCommandFactory<Transaction, *, *>) =
		addRoute(method = HttpMethod.Get, commandFactory = commandFactory)


	@ContextDsl
	fun Route.head(path: String, commandFactory: BakuCommandFactory<Transaction, *, *>) =
		addRoute(method = HttpMethod.Head, path = path, commandFactory = commandFactory)


	@ContextDsl
	fun Route.head(commandFactory: BakuCommandFactory<Transaction, *, *>) =
		addRoute(method = HttpMethod.Head, commandFactory = commandFactory)


	@ContextDsl
	fun Route.post(path: String, commandFactory: BakuCommandFactory<Transaction, *, *>) =
		addRoute(method = HttpMethod.Post, path = path, commandFactory = commandFactory)


	@ContextDsl
	fun Route.post(commandFactory: BakuCommandFactory<Transaction, *, *>) =
		addRoute(method = HttpMethod.Post, commandFactory = commandFactory)


	@ContextDsl
	private fun Route.handle(commandFactory: BakuCommandFactory<Transaction, *, *>) {
		commandRoutes += BakuCommandRoute(
			factory = commandFactory,
			route = this
		)
	}


	@ContextDsl
	fun Route.options(path: String, commandFactory: BakuCommandFactory<Transaction, *, *>) =
		addRoute(method = HttpMethod.Options, path = path, commandFactory = commandFactory)


	@ContextDsl
	fun Route.options(commandFactory: BakuCommandFactory<Transaction, *, *>) =
		addRoute(method = HttpMethod.Options, commandFactory = commandFactory)


	@ContextDsl
	fun Route.patch(path: String, commandFactory: BakuCommandFactory<Transaction, *, *>) =
		addRoute(method = HttpMethod.Patch, path = path, commandFactory = commandFactory)


	@ContextDsl
	fun Route.patch(commandFactory: BakuCommandFactory<Transaction, *, *>) =
		addRoute(method = HttpMethod.Patch, commandFactory = commandFactory)


	@ContextDsl
	fun Route.put(path: String, commandFactory: BakuCommandFactory<Transaction, *, *>) =
		addRoute(method = HttpMethod.Put, path = path, commandFactory = commandFactory)


	@ContextDsl
	fun Route.put(commandFactory: BakuCommandFactory<Transaction, *, *>) =
		addRoute(method = HttpMethod.Put, commandFactory = commandFactory)


	inner class Commands internal constructor() {

		internal val handlers: MutableList<BakuCommandHandler<Transaction, *, *>> = mutableListOf()


		operator fun <Command : BakuCommand, Result : Any> BakuCommandFactory<Transaction, Command, Result>.invoke(
			handler: Transaction.() -> (suspend (command: Command) -> Result)
		) {
			handlers += BakuCommandHandler(
				factory = this,
				handler = handler
			)
		}
	}


	inner class Entities internal constructor() {

		@PublishedApi
		internal val resolvers: MutableMap<KClass<out EntityId>, suspend Transaction.(ids: Set<EntityId>) -> Flow<Entity>> = mutableMapOf()


		inline fun <reified Id : EntityId> resolve(noinline resolver: suspend Transaction.(ids: Set<Id>) -> Flow<Entity>) {
			@Suppress("UNCHECKED_CAST")
			if (resolvers.putIfAbsent(Id::class, resolver as suspend Transaction.(ids: Set<EntityId>) -> Flow<Entity>) != null) {
				error("Cannot register multiple entity resolvers for ${Id::class}")
			}
		}
	}
}
