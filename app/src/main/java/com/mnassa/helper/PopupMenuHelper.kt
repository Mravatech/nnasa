package com.mnassa.helper

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.mnassa.R
import com.mnassa.domain.model.ExpirationType
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.*
import com.mnassa.translation.fromDictionary

/**
 * Created by Peter on 30.03.2018.
 */
class PopupMenuHelper(private val dialogHelper: DialogHelper) {

    suspend fun showMyPostMenu(
            view: View,
            post: PostModel,
            onEditPost: () -> Unit,
            onDeletePost: () -> Unit,
            onPromotePost: () -> Unit,
            changeStatus: (ExpirationType) -> Unit = { }
    ) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.post_edit, popup.menu)
        popup.menu.findItem(R.id.action_post_edit).title = fromDictionary(R.string.need_action_edit)
        popup.menu.findItem(R.id.action_post_delete).title = fromDictionary(R.string.need_action_delete)
        popup.menu.findItem(R.id.action_post_promote).title = fromDictionary(R.string.post_promote_menu)
        popup.menu.findItem(R.id.action_post_close).title = fromDictionary(R.string.post_close)
        popup.menu.findItem(R.id.action_post_fulfill).title = fromDictionary(R.string.post_fulfill)
        if (!post.canBePromoted()) {
            popup.menu.removeItem(R.id.action_post_promote)
        }
        if (!post.canBeEdited) {
            popup.menu.removeItem(R.id.action_post_edit)
        }
        if (!post.statusCanBeChanged) {
            popup.menu.removeItem(R.id.action_post_close)
            popup.menu.removeItem(R.id.action_post_fulfill)
        }

        val promotionPrice = post.getPromotionPrice()

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_post_edit -> onEditPost()
                R.id.action_post_delete -> dialogHelper.showConfirmPostRemovingDialog(view.context, onDeletePost)
                R.id.action_post_promote -> dialogHelper.showConfirmPostPromotingDialog(view.context, promotionPrice, onPromotePost)
                R.id.action_post_close -> changeStatus(ExpirationType.CLOSED)
                R.id.action_post_fulfill -> changeStatus(ExpirationType.FULFILLED)

            }
            true
        }

        popup.show()
    }

    fun showMyCommentMenu(view: View, onEditComment: () -> Unit, onDeleteComment: () -> Unit) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.comment_edit, popup.menu)
        popup.menu.findItem(R.id.action_comment_edit).title = fromDictionary(R.string.posts_comment_edit)

        val deleteSpan = SpannableString(fromDictionary(R.string.posts_comment_delete))
        val deleteTextColor = ContextCompat.getColor(view.context, R.color.red)
        deleteSpan.setSpan(ForegroundColorSpan(deleteTextColor), 0, deleteSpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        popup.menu.findItem(R.id.action_comment_delete).title = deleteSpan

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_comment_edit -> onEditComment()
                R.id.action_comment_delete -> onDeleteComment()
            }
            true
        }

        popup.show()
    }

    fun showPostMenu(view: View, post: PostModel, onRepost: () -> Unit, onReport: () -> Unit) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.post_view, popup.menu)
        popup.menu.findItem(R.id.action_post_repost).title = fromDictionary(R.string.need_action_repost)
        popup.menu.findItem(R.id.action_post_report).title = fromDictionary(R.string.need_action_report)

        if (!post.canBeShared) {
            popup.menu.removeItem(R.id.action_post_repost)
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_post_repost -> onRepost()
                R.id.action_post_report -> onReport()
            }
            true
        }

        popup.show()
    }

    suspend fun showConnectedAccountMenu(view: View,
                                 account: ShortAccountModel,
                                 onChat: () -> Unit,
                                 onProfile: () -> Unit,
                                 onDisconnect: () -> Unit) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.connections_item, popup.menu)
        popup.menu.findItem(R.id.action_connections_send_message).title = fromDictionary(R.string.tab_connections_all_item_send_message)
        popup.menu.findItem(R.id.action_connections_view_profile).title = fromDictionary(R.string.tab_connections_all_item_view_profile)

        val disconnectSpan = SpannableString(fromDictionary(R.string.tab_connections_all_item_disconnect))
        val disconnectTextColor = ContextCompat.getColor(view.context, R.color.red)
        disconnectSpan.setSpan(ForegroundColorSpan(disconnectTextColor), 0, disconnectSpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        popup.menu.findItem(R.id.action_connections_disconnect).title = disconnectSpan

        if (!account.canDisconnect()) {
            popup.menu.removeItem(R.id.action_connections_disconnect)
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_connections_send_message -> onChat()
                R.id.action_connections_view_profile -> onProfile()
                R.id.action_connections_disconnect -> onDisconnect()
            }
            true
        }

        popup.show()
    }

    fun showConnectionsTabMenu(view: View, openRecommendedConnectionsScreen: () -> Unit, openSentRequestsScreen: () -> Unit, openArchivedConnectionsScreen: () -> Unit) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.connections_main, popup.menu)
        popup.menu.findItem(R.id.action_recommended_connections).title = fromDictionary(R.string.tab_connections_recommended)
        popup.menu.findItem(R.id.action_sent_requests).title = fromDictionary(R.string.tab_connections_new_requests)
        popup.menu.findItem(R.id.action_archived).title = fromDictionary(R.string.tab_connections_rejected)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_recommended_connections -> openRecommendedConnectionsScreen()
                R.id.action_sent_requests -> openSentRequestsScreen()
                R.id.action_archived -> openArchivedConnectionsScreen()
            }
            true
        }

        popup.show()
    }

    fun showGroupsMenu(view: View, openRequests: () -> Unit) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.groups, popup.menu)
        popup.menu.findItem(R.id.action_group_requests).title = fromDictionary(R.string.groups_requests)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_group_requests -> openRequests()
            }
            true
        }

        popup.show()
    }

    fun showGroupItemMenu(view: View, onLeave: () -> Unit, onDetails: () -> Unit) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.group_item, popup.menu)
        popup.menu.findItem(R.id.action_leave_group).title = fromDictionary(R.string.group_leave)
        popup.menu.findItem(R.id.action_open_details).title = fromDictionary(R.string.group_menu_details)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_leave_group -> onLeave()
                R.id.action_open_details -> onDetails()
            }
            true
        }

        popup.show()
    }

    fun showGroupMemberMenu(view: View, group: GroupModel, account: ShortAccountModel, onRemove: () -> Unit, onAdmin: () -> Unit, onMember: () -> Unit) {
        if (group.isAdmin) {
            val isAdminAccount = group.admins.contains(account.id)

            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.group_member, popup.menu)
            popup.menu.findItem(R.id.action_group_member_remove)?.title = fromDictionary(R.string.group_remove_member)
            popup.menu.findItem(R.id.action_group_member_admin)?.title = if (isAdminAccount)
                fromDictionary(R.string.group_member_un_admin) else fromDictionary(R.string.group_member_admin)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_group_member_remove -> onRemove()
                    R.id.action_group_member_admin -> if (isAdminAccount) onMember() else onAdmin()
                }
                true
            }

            popup.show()
        }
    }

    fun showGroupPostItemMenu(view: View, onRemove: () -> Unit) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.post_item, popup.menu)
        popup.menu.findItem(R.id.action_post_remove)?.title = fromDictionary(R.string.group_post_remove)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_post_remove -> onRemove()
            }
            true
        }
        popup.show()

    }

    fun showGroupInviteMenu(view: View, onAccept: () -> Unit, onDecline: () -> Unit) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.group_invite, popup.menu)
        popup.menu.findItem(R.id.action_group_invite_accept)?.title = fromDictionary(R.string.group_invite_apply)
        popup.menu.findItem(R.id.action_group_invite_decline)?.title = fromDictionary(R.string.group_invite_decline)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_group_invite_accept -> onAccept()
                R.id.action_group_invite_decline -> onDecline()
            }
            true
        }
        popup.show()
    }

}