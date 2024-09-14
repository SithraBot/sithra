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
import kotlinx.serialization.encodeToString
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
        logging = LoggingConfig(logger = Logger.Empty),
    )

    suspend fun chat(messages: List<ChatMessage>): ChatCompletion {
        return openai.chatCompletion(
            ChatCompletionRequest(
                model = ModelId(mainConfig.openai.model),
                temperature = 0.8,
                maxTokens = 2000,
                messages = messages,
                tools = Tools.vTools.map {
                    Tool(ToolType.Function, it)
                },
                responseFormat = ChatResponseFormat.JsonObject
            )
        )
    }

    @Subscribe
    suspend fun ClientWebSocketSession.messageVisitor(event: MessageEvent) {
        if (event.message.any { it.type == "at" && it.data.qq == mainConfig.onebot.selfId }) {
            val messageContent = event.message.joinToString { it.data.text ?: it.data.qq?.toString() ?: "" }
            val currentMessageChain = messageChain.getOrPut(event.channelId.id) {
                mutableListOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = "你是群管理员，执行高危操作前请先向用户确认再进行。你的QQ为${mainConfig.onebot.selfId}，用户的消息总是会包含你的QQ号，请无视。"
                    )
                )
            }
            currentMessageChain.add(ChatMessage(role = ChatRole.User, content = messageContent))
            var assistMessage: ChatMessage = chat(currentMessageChain).choices.firstOrNull()?.message ?: return
            var toolCalls = assistMessage.toolCalls
            while (toolCalls != null) {
                val toolMessages: MutableList<ChatMessage> = mutableListOf(assistMessage)
                toolCalls.forEach {
                    if (it is ToolCall.Function) {
                        val result = Tools.callTool(this, it, event) ?: "success"
                        toolMessages.add(
                            ChatMessage(
                                name = "AAA",
                                role = ChatRole.Tool,
                                content = result,
                                toolCallId = it.id
                            )
                        )
                    }
                }
                currentMessageChain.addAll(toolMessages)
                assistMessage = chat(currentMessageChain).choices.firstOrNull()?.message ?: return
                toolCalls = assistMessage.toolCalls
            }
            currentMessageChain.add(assistMessage)
            if (assistMessage.content?.isEmpty() == false) {
                event.run {
                    reply(assistMessage.content!!)
                }
            }
        }
    }
}