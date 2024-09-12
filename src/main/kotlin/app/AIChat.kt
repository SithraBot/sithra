package top.ninnana.app

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import io.ktor.client.plugins.websocket.*
import top.ninnana.plugin.Plugin
import top.ninnana.bean.MessageEvent
import top.ninnana.handle.Subscribe
import top.ninnana.mainConfig

class AIChat : Plugin() {
    val messageChain: MutableMap<Long, MutableList<ChatMessage>> = mutableMapOf()

    val openai = OpenAI(
        token = mainConfig.openai.token,
        host = OpenAIHost(baseUrl = mainConfig.openai.baseUrl),
        logging = LoggingConfig(logger = Logger.Empty)
    )

    @Subscribe
    suspend fun messageVisitor(session: ClientWebSocketSession, event: MessageEvent) {
        if (event.message.any { it.type == "at" && it.data.qq == mainConfig.onebot.selfId }) {
            val messageContent = event.message.filter { it.type == "text" }.joinToString { it.data.text ?: "" }
            val currentMessageChain = messageChain.getOrPut(event.channelId.id) { mutableListOf() }
            currentMessageChain.add(ChatMessage(role = ChatRole("user"), content = messageContent))
            val response = openai.chatCompletion(
                ChatCompletionRequest(
                    model = ModelId(mainConfig.openai.model),
                    messages = currentMessageChain
                )
            )
            val assistMessage = response.choices.firstOrNull()?.message
            if (assistMessage != null) {
                currentMessageChain.add(assistMessage)
                if (assistMessage.content != null) {
                    event.replay(assistMessage.content!!, session)
                }
            }
        }
    }

    override suspend fun load() {
        super.load()
        logger.info("AIChat 插件已加载")
    }
}