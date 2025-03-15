package org.sithra.sithrabot.config

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.io.File
import java.util.Properties

class BaseConfig(file: File) {
    val aiAdapterBuilderClass: String

    companion object {
        const val DEFAULT_CONFIG = """ai.adapter.builder-class=org.sithra.sithrabot.builtin.builders.OpenAIAdapterBuilder"""
    }

    init {
        file.reader().use {
            Properties().apply { load(it) }.let {
                aiAdapterBuilderClass = it.getProperty("ai.adapter.builder-class")
            }
        }
    }
}