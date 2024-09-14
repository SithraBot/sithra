# Sithra

OneBot AI ChatBot. Extensible AI chat operation framework.

## How does it work

Use Kotlin's reflection capabilities and Tools API to achieve the purpose of registering functions for automatic invocation by AI.

Provide AI with the ability to produce side effects in context.

## Demo

```kotlin
object MyToolBox : Plugin(), ToolBox {
    override fun onLoad() {
        registerTools()
    }
    
    @Tool("Ban or unban users known to QQ")
    @WithEvent
    suspend fun ban(
        session: ClientWebSocketSession,
        event: Event?,
        @Description("The muting duration (unit: seconds), set to 0 to cancel the muting") duration: Int = 60,
        @Description("QQ account of the banned person") id: Long,
    ): String {
        if (event is Proven && event.group_id != null) {
            if (!event.isAdmin) return "PERMISSION DENIED"
            session.send(SetGroupBan(event.group_id!!, id, duration))
        } else return "NOT IN GROUP CHAT"
        return "OPERATION SUCCESSFUL"
    }
}

```

This code snippet provides the AI with the ability to ban users. Simply use natural language instructions to direct the AI to achieve the purpose of banning.

## Why

Using natural language commands can reduce the cost of use, making it easier for users to get started, and it can understand complex user instructions to automatically create processes and invoke functions, giving it more possibilities. 

Even though there may be unpredictable issues with using natural language processing commands, the benefits it provides outweigh the drawbacks.
