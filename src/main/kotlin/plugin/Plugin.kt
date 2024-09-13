package top.ninnana.plugin

import io.klogging.logger

abstract class Plugin {
    val logger = logger(this::class)
    open fun onLoad() {}
    open fun onUnload() {}
}