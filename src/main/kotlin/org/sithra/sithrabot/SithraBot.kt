package org.sithra.sithrabot

import org.sithra.synthetic.context.Context
import io.github.oshai.kotlinlogging.KotlinLogging;
import org.sithra.sithrabot.loader.AIAdapterLoader
import org.sithra.sithrabot.config.BaseConfig
import org.sithra.synthetic.store.casing.InMemoryMessagesStore
import java.io.File

class SithraBot {
    private val logger = KotlinLogging.logger {}
    private val baseConfig: BaseConfig = BaseConfig(File("./config.properties"))
    private val aiAdapterLoader = AIAdapterLoader(baseConfig.aiAdapterBuilderClass)
    private val aiMessages = InMemoryMessagesStore()
    val aiContext = Context {
        apiAdapter = aiAdapterLoader.adapter
        messagesStore = aiMessages
    }

    suspend fun start() {
        logger.info { "启动中..." }
    }
}