package top.ninnana.bean

import kotlinx.serialization.*

@Serializable
abstract class Event {
    abstract val time: Long
    abstract val self_id: Long
    abstract val post_type: String
}

interface Proven {
    val group_id: Long?
    val user_id: Long
    val channelId: ChannelId
        get() = if (group_id != null) {
            GroupId(group_id!!)
        } else {
            PrivateId(user_id)
        }
    val isPrivate
        get() = group_id == null

    val isGroup
        get() = group_id != null
}