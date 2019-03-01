package com.mnassa.domain.model

import java.io.Serializable
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
interface NotificationModel : Model {
    val createdAt: Date
    val text: String
    val type: String
    val extra: NotificationExtra
    var isOld: Boolean
}

interface NotificationExtra : Serializable {
    val author: ShortAccountModel?
    val post: Post?
    var reffered: UserReferred?
    var recommended: UserRecommended?
    var group: GroupModel?
    val eventName: String?
    val ticketsPrice: String?
    val totalPrice: String?
    val attendee: String?
    val event: Event?
    val newInviteNumber: Int?

    interface Post : Model {
        var author: ShortAccountModel?
        var type: String
        var text: String
    }

    interface Event : Model {
        var author: ShortAccountModel?
        var title: String
    }

    interface UserReferred : Model {
        var userName: String
        //
        var personalInfo: PersonalAccountDiffModel?
        var organizationInfo: OrganizationAccountDiffModel?
    }

    interface UserRecommended : Model {
        var userName: String
        //
        var personalInfo: PersonalAccountDiffModel?
        var organizationInfo: OrganizationAccountDiffModel?
    }
}
