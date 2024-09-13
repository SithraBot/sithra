package top.ninnana.app.ai

import com.aallam.openai.api.chat.FunctionTool
import com.aallam.openai.api.chat.ToolCall
import com.aallam.openai.api.core.Parameters
import io.klogging.logger
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import top.ninnana.bean.Event
import kotlin.reflect.*
import kotlin.reflect.full.*

typealias ToolFunction = suspend (ClientWebSocketSession, Event?, Map<KParameter, Any?>) -> String?

data class ToolInstance(
    val function: ToolFunction,
    val params: List<KParameter>,
    val tool: FunctionTool
)

object Tools {
    val logger = logger(Tools::class)

    val tools: MutableMap<String, ToolInstance> = mutableMapOf()

    val vTools: List<FunctionTool>
        get() = tools.map { it.value.tool }

    fun registerTool(function: KFunction<*>) = try {
        val description = function.findAnnotation<Tool>()!!.description
        val name = function.name
        val (parameters, eventParameter, sessionParameter) = if (function.findAnnotation<WithEvent>() != null) {
            Triple(
                function.parameters.subList(2, function.parameters.size),
                function.parameters[1],
                function.parameters.first(),
            )
        } else {
            Triple(
                function.parameters.subList(1, function.parameters.size),
                null,
                function.parameters.first(),
            )
        }
        val params = parameters.toSchema()
        val toolFunction: ToolFunction =
            { session: ClientWebSocketSession, event: Event?, args: Map<KParameter, Any?> ->
                val argsMap = if (eventParameter != null) {
                    args + mapOf(eventParameter to event)
                } else {
                    args
                } + mapOf(sessionParameter to session)
                if (function.isSuspend) function.callSuspendBy(argsMap) as? String
                else function.callBy(argsMap) as? String
            }
        tools[name] = ToolInstance(
            toolFunction,
            parameters,
            FunctionTool(name, params, description)
        )
    } catch (e: Exception) {
        runBlocking {
            logger.error("Failed to register tool: ${e.message}")
        }
    }

    fun registerTool(obj: ToolBox) = try {
        val functions = obj::class.functions.filter { it.findAnnotation<Tool>() != null }
        for (function in functions) {
            val description = function.findAnnotation<Tool>()!!.description
            val name = function.name
            val (parameters, eventParameter, sessionParameter) = if (function.findAnnotation<WithEvent>() != null) {
                Triple(
                    function.parameters.subList(3, function.parameters.size),
                    function.parameters[2],
                    function.parameters[1],
                )
            } else {
                Triple(
                    function.parameters.subList(2, function.parameters.size),
                    null,
                    function.parameters[1],
                )
            }
            val params = parameters.toSchema()
            val thisParams = function.parameters.first()
            val toolFunction: ToolFunction =
                { session: ClientWebSocketSession, event: Event?, args: Map<KParameter, Any?> ->
                    val argsMap = args.filterKeys { it != thisParams }.let {
                        if (eventParameter != null) {
                            it + mapOf(eventParameter to event)
                        } else {
                            it
                        } + mapOf(thisParams to obj) + mapOf(sessionParameter to session)
                    }
                    if (function.isSuspend) function.callSuspendBy(argsMap) as? String
                    else function.callBy(argsMap) as? String
                }
            tools[name] = ToolInstance(
                toolFunction,
                parameters,
                FunctionTool(name, params, description)
            )
        }
    } catch (e: Exception) {
        runBlocking {
            logger.error("Failed to register tool: ${e.message}")
        }
    }

    suspend fun callTool(
        session: ClientWebSocketSession,
        toolName: String,
        args: Map<String, Any>,
        event: Event? = null
    ): String? {
        return try {
            val tool = tools[toolName] ?: return null
            val params = tool.params.associate { it to args[it.name] }
            tool.function(session, event, params)
        } catch (e: Exception) {
            logger.error("Failed to call tool: ${e.message}")
            null
        }
    }

    suspend fun callTool(session: ClientWebSocketSession, toolCall: ToolCall.Function, event: Event? = null): String? {
        val args = Json.decodeFromString<JsonObject>(toolCall.function.arguments)
        val name = toolCall.function.name
        val function = tools[name] ?: return null
        val params: Map<KParameter, Any?> = function.params.associate {
            val valueJson: JsonPrimitive = args[it.name!!] as? JsonPrimitive ?: return@associate it to null
            val value: Any = valueJson.intOrNull
                ?: valueJson.longOrNull
                ?: valueJson.doubleOrNull
                ?: valueJson.booleanOrNull
                ?: valueJson.content
            it to value
        }
        return function.function(session, event, params)
    }
}

fun List<KParameter>.toSchema(): Parameters {
    return Parameters.buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            this@toSchema.forEach {
                val paramDescription = it.findAnnotation<Description>()?.description ?: ""
                putJsonObject(it.name!!) {
                    val type = when {
                        it.type.isSubtypeOf(typeOf<Boolean>()) -> "boolean"
                        it.type.isSubtypeOf(typeOf<Int>()) -> "integer"
                        it.type.isSubtypeOf(typeOf<Number>()) -> "number"
                        it.type.isSubtypeOf(typeOf<String>()) -> "string"
                        it.type.isSubtypeOf(typeOf<List<*>>()) -> "array"
                        else -> "object"
                    }
                    put("type", type)
                    put("description", paramDescription)
                }
            }
        }
        putJsonArray("required") {
            this@toSchema.forEach {
                if (!it.isOptional) add(it.name!!)
            }
        }
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Tool(val description: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class WithEvent

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Description(val description: String)