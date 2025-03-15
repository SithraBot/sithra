package org.sithra.sithrabot.loader

import io.github.oshai.kotlinlogging.KotlinLogging
import org.sithra.sithrabot.builders.AIAdapterBuilder
import org.sithra.synthetic.adapter.IAdapter
import java.io.File
import kotlin.reflect.full.createInstance
import kotlin.system.exitProcess

class AIAdapterLoader(clazz: String) {
    private val logger = KotlinLogging.logger {}
    private val builderCtx = try {
        val builderClass = ClassLoader.getSystemClassLoader()
            .loadClass(clazz).kotlin
        builderClass.createInstance() as AIAdapterBuilder<*> to builderClass.simpleName
    } catch (e: Exception) {
        throw IllegalStateException("无法加载 AI 适配器构建器。", e)
    }
    private var innerAdapter: IAdapter? = null
    val adapter: IAdapter
        get() =
            if (innerAdapter == null) {
                reload()
                innerAdapter!!
            } else {
                innerAdapter!!
            }
    fun reload(config: String) {
        val (builder, builderName) = builderCtx
        innerAdapter = builder.reload(config)
    }
    fun reload() {
        val (builder, builderName) = builderCtx
        logger.info { "正在使用 $builderName 构建 AI 适配器。" }
        var config = builder.defaultConfig()
        val file = File("./${builder.configFileName()}")
        if (file.exists()) {
            config = file.readText()
        } else {
            file.createNewFile()
            file.writeText(config)
            logger.info { "请于 ./${builder.configFileName()} 中填写配置后再次启动。" }
            exitProcess(0)
        }
        innerAdapter = builder.build(config)
    }
}