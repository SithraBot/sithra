package org.sithra.sithrabot.config

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.util.Properties

class BaseConfig(file: File) {
    val aiAdapterBuilderClass: String

    companion object {
        const val DEFAULT_CONFIG =
            """ai.adapter.builder-class=org.sithra.sithrabot.builtin.builders.OpenAIAdapterBuilder"""

        fun default(): BaseConfig {
            val logger = KotlinLogging.logger {}
            val file = File("./config.properties")
            if (file.exists()) {
                return BaseConfig(file)
            } else {
                logger.info { "配置文件不存在，使用默认配置。" }
                file.writer().use {
                    it.write(DEFAULT_CONFIG)
                }
                return BaseConfig(file)
            }
        }
    }

    init {
        file.reader().use {
            Properties().apply { load(it) }.let {
                aiAdapterBuilderClass = it.getProperty("ai.adapter.builder-class")
            }
        }
    }
}