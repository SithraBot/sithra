package org.sithra.sithrabot.builtin.builders

import com.charleskorn.kaml.Yaml
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sithra.sithrabot.builders.AIAdapterBuilder
import org.sithra.synthetic.adapter.IAdapter
import org.sithra.synthetic.adapter.OpenAIAdapter

class OpenAIAdapterBuilder : AIAdapterBuilder<OpenAIAdapter> {
    @Serializable
    data class Config(
        @SerialName("api-key")
        val apiKey: String,
        @SerialName("base-url")
        val baseUrl: String
    )

    val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
        }
    }

    override fun build(config: String): OpenAIAdapter {
        val configData = Yaml.default.decodeFromString(Config.serializer(), config)
        return OpenAIAdapter {
            baseUrl = Url(configData.baseUrl)
            token = configData.apiKey
            httpClient = client
        }
    }

    override fun defaultConfig(): String {
        return """
            api-key: "[你的API Key]"
            base-url: "https://api.openai.com/v1"
        """.trimIndent()
    }

    override fun configFileName(): String {
        return "openai.yaml"
    }

    override fun reload(config: String): OpenAIAdapter {
        return build(config)
    }
}