package top.ninnana.app.ai

import com.aallam.openai.api.chat.*
import com.aallam.openai.api.chat.Tool
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.runBlocking
import top.ninnana.plugin.Plugin
import top.ninnana.bean.MessageEvent
import top.ninnana.handle.EventListener
import top.ninnana.handle.Subscribe
import top.ninnana.mainConfig
import top.ninnana.utils.json

object AIChat : Plugin(), EventListener {
    override fun onLoad() = runBlocking {
        registerListener()
        logger.info("AIChat 已加载")
    }

    val messageChain: MutableMap<Long, MutableList<ChatMessage>> = mutableMapOf()

    val openai = OpenAI(
        token = mainConfig.openai.token,
        host = OpenAIHost(baseUrl = mainConfig.openai.baseUrl),
        logging = LoggingConfig(logger = Logger.Empty)
    )

    @Subscribe
    suspend fun ClientWebSocketSession.messageVisitor(event: MessageEvent) {
        if (event.message.any { it.type == "at" && it.data.qq == mainConfig.onebot.selfId }) {
            val messageContent = event.message.filter { it.type == "text" }.joinToString { it.data.text ?: "" }
            val currentMessageChain = messageChain.getOrPut(event.channelId.id) {
                mutableListOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = "你是QQ群聊管理员,请帮助用户管理群聊。"
                    )
                )
            }
            currentMessageChain.add(ChatMessage(role = ChatRole.User, content = messageContent))
            suspend fun chat(messages: List<ChatMessage>): ChatCompletion {
                return openai.chatCompletion(
                    ChatCompletionRequest(
                        model = ModelId(mainConfig.openai.model),
                        temperature = 0.1,
                        topP = 1.0,
                        maxTokens = 8000,
                        messages = messages,
                        tools = Tools.vTools.map {
                            Tool(ToolType.Function, it)
                        }
                    )
                )
            }

            val response = chat(currentMessageChain)
            var assistMessage = response.choices.firstOrNull()?.message
            assistMessage?.toolCalls?.forEach {
                if (it is ToolCall.Function) {
                    val result = Tools.callTool(this, it, event) ?: "success"
                    currentMessageChain.add(assistMessage!!)
                    currentMessageChain.add(
                        ChatMessage(
                            role = ChatRole.Tool,
                            content = result,
                            toolCallId = it.id,
                        )
                    )
                    val responseWithToolCall = chat(currentMessageChain)
                    assistMessage = responseWithToolCall.choices.firstOrNull()?.message
                }
            }
            if (assistMessage != null) {
                currentMessageChain.add(assistMessage!!)
                if (assistMessage!!.content != null) {
                    event.run {
                        reply(assistMessage!!.content!!)
                    }
                }
            }
        }
    }
}