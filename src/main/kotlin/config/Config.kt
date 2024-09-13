package top.ninnana.config

import kotlinx.serialization.*
import top.ninnana.plugin.Plugin
import kotlin.reflect.full.createInstance

@Serializable
data class Config(
    val plugins: MutableList<String>,
    val onebot: OneBot,
    val openai: OpenAI
) {
    @Serializable
    data class OneBot(
        val token: String,
        val selfId: Long,
        val host: String,
        val port: Int,
        val path: String,
    )

    @Serializable
    data class OpenAI(
        val token: String,
        val model: String,
        val baseUrl: String
    )

    fun getPluginsInstance(): List<Plugin> {
        return plugins.map {
            val clazz = this::class.java.classLoader.loadClass(it)
            clazz.kotlin.objectInstance as Plugin
        }
    }
}