package com.mnassa.screen.invite

import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.PostModel
import java.io.Serializable

/**
 * Created by Peter on 6/7/2018.
 */
sealed class InviteSource : Serializable {
    class Group(val group: GroupModel) : InviteSource()
    class Post(val post: PostModel) : InviteSource()
    class Event(val event: EventModel) : InviteSource()
    class Manual : InviteSource()
    class Notification : InviteSource()
}

data class InviteSourceHolder(var source: InviteSource? = null)