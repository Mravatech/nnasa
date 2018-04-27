package com.mnassa.helper

import android.support.v4.content.ContextCompat
import android.support.v7.widget.PopupMenu
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import com.mnassa.R
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.canBeShared
import com.mnassa.translation.fromDictionary

/**
 * Created by Peter on 30.03.2018.
 */
class PopupMenuHelper(private val dialogHelper: DialogHelper) {

    fun showMyPostMenu(view: View, onEditPost: () -> Unit, onDeletePost: () -> Unit) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.post_edit, popup.menu)
        popup.menu.findItem(R.id.action_post_edit).title = fromDictionary(R.string.need_action_edit)
        popup.menu.findItem(R.id.action_post_delete).title = fromDictionary(R.string.need_action_delete)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_post_edit -> onEditPost()
                R.id.action_post_delete -> dialogHelper.showConfirmPostRemovingDialog(view.context, onDeletePost)
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

    fun showConnectedAccountMenu(view: View, onChat: () -> Unit, onProfile: () -> Unit, onDisconnect: () -> Unit) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.connections_item, popup.menu)
        popup.menu.findItem(R.id.action_connections_send_message).title = fromDictionary(R.string.tab_connections_all_item_send_message)
        popup.menu.findItem(R.id.action_connections_view_profile).title = fromDictionary(R.string.tab_connections_all_item_view_profile)

        val disconnectSpan = SpannableString(fromDictionary(R.string.tab_connections_all_item_disconnect))
        val disconnectTextColor = ContextCompat.getColor(view.context, R.color.red)
        disconnectSpan.setSpan(ForegroundColorSpan(disconnectTextColor), 0, disconnectSpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        popup.menu.findItem(R.id.action_connections_disconnect).title = disconnectSpan

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
}