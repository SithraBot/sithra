package top.ninnana.config

import kotlinx.serialization.Serializable

@Serializable
data class Config(
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
}