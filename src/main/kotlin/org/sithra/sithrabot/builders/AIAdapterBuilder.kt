package org.sithra.sithrabot.builders

import org.sithra.synthetic.adapter.IAdapter

interface AIAdapterBuilder<D : IAdapter> {
    fun build(config: String): D
    fun defaultConfig(): String
    fun configFileName(): String
    fun reload(config: String): D
}