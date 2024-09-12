package top.ninnana.bean

import kotlinx.serialization.*

interface APIData

@Serializable
abstract class API<D : APIData> {
    abstract val params: D
    abstract val action: String
    abstract val echo: String
}

@Serializable
class SendPrivateMsg : API<SendPrivateMsgData> {
    constructor(
        user_id: Long,
        message: List<MessageNode>,
        auto_escape: Boolean = false,
        echo: String = "SendPrivateMsg"
    ) {
        this.echo = echo
        this.params = SendPrivateMsgData(user_id, message, auto_escape)
    }

    constructor(data: SendPrivateMsgData, echo: String = "SendPrivateMsg") {
        this.echo = echo
        this.params = data
    }

    override val echo: String
    override val params: SendPrivateMsgData
    override val action: String = "send_private_msg"
}


@Serializable
data class SendPrivateMsgData(
    val user_id: Long,
    val message: List<MessageNode>,
    val auto_escape: Boolean = false
) : APIData

@Serializable
class SendGroupMsg : API<SendGroupMsgData> {
    constructor(
        group_id: Long,
        message: List<MessageNode>,
        auto_escape: Boolean = false,
        echo: String = "SendPrivateMsg"
    ) {
        this.echo = echo
        this.params = SendGroupMsgData(group_id, message, auto_escape)
    }

    constructor(data: SendGroupMsgData, echo: String = "SendGroupMsg") {
        this.echo = echo
        this.params = data
    }

    override val echo: String
    override val params: SendGroupMsgData
    override val action: String = "send_group_msg"
}


@Serializable
data class SendGroupMsgData(
    val group_id: Long,
    val message: List<MessageNode>,
    val auto_escape: Boolean = false
) : APIData
