package top.ninnana.plugin

import io.klogging.logger
import top.ninnana.handle.registerEventHandle

abstract class Plugin {
    val logger = logger(this::class)
    open suspend fun load() {
        this.registerEventHandle()
    }
}