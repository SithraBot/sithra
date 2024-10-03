package top.ninnana.loader

import top.ninnana.plugin.Plugin
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarFile
import kotlin.reflect.KClass

class PluginLoader(path: String) : ClassLoader() {
    val directory = File(path)

    fun loadFromFile(file: File): KClass<Plugin> {
        val jar = JarFile(file)
        val entryPoint = jar.manifest.getAttributes("Sithra").getValue("Entry-Point")
        val classLoader = URLClassLoader(arrayOf(file.toURI().toURL()), this)
        return classLoader.loadClass(entryPoint).kotlin as? KClass<Plugin> ?: throw Error("Not Plugin: $entryPoint")
    }

    fun loadAll(): Set<KClass<Plugin>> {
        val jarFiles = directory.listFiles { file: File -> file.isFile && file.extension == "jar" } ?: arrayOf()
        val pluginSet = mutableSetOf<KClass<Plugin>>()
        jarFiles.forEach {
            pluginSet.add(loadFromFile(it))
        }
        return pluginSet
    }
}