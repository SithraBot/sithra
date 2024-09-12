package top.ninnana

import com.charleskorn.kaml.Yaml
import io.klogging.config.ANSI_CONSOLE
import io.klogging.config.loggingConfiguration
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import okio.source
import top.ninnana.config.Config
import top.ninnana.handle.EventHandleManager
import top.ninnana.utils.decodeToEvent
import java.io.File


val client = HttpClient(CIO) {
    install(WebSockets)
}

val mainConfigFile = File(".").listFiles { file: File ->
    val extension = file.extension.uppercase()
    val name = file.nameWithoutExtension
    name == "config" && (extension == "YAML" || extension == "YML")
}?.firstOrNull()
val mainConfig = Yaml.default.decodeFromSource(Config.serializer(), mainConfigFile!!.source())

suspend fun main() {
    loggingConfiguration {
        ANSI_CONSOLE()
    }

    mainConfig.getPluginsInstance().forEach {
        it.load()
    }
    client.ws(
        method = HttpMethod.Get,
        host = mainConfig.onebot.host,
        port = mainConfig.onebot.port,
        path = mainConfig.onebot.path,
        request = {
            header("User-Agent", "CQHttp/4.15.0")
            header("X-Self-ID", mainConfig.onebot.selfId.toString())
            header(
                "Authorization", "Bearer ${mainConfig.onebot.token}"
            )
        }) {
        incoming.consumeEach {
            listenEvents(it)
        }
    }
}

suspend fun ClientWebSocketSession.listenEvents(frame: Frame) {
    when (frame) {
        is Frame.Text -> {
            val event = frame.readText().decodeToEvent()
            if (event != null) {
                EventHandleManager.handleEvent(this, event)
            }
        }

        else -> {}
    }
}