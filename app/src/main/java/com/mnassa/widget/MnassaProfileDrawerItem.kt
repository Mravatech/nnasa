package com.mnassa.widget

import android.widget.ImageView
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.formattedPosition
import com.mnassa.translation.fromDictionary

/**
 * Created by Peter on 4/4/2018.
 */
class MnassaProfileDrawerItem : ProfileDrawerItem() {

    lateinit var account: ShortAccountModel

    private lateinit var iconInternal: ImageHolder
    override fun getIcon(): ImageHolder = iconInternal

    fun withAccount(shortAccountModel: ShortAccountModel): MnassaProfileDrawerItem {
        account = shortAccountModel

        withNameShown(true)

        withName(shortAccountModel.formattedName)
        withEmail(shortAccountModel.formattedPosition.toString().takeIf { it.isNotBlank() }
                ?: fromDictionary(R.string.position_not_specified))
        withIdentifier(shortAccountModel.id.hashCode().toLong())

        iconInternal = MnassaImageHolder(shortAccountModel.avatar)

        return this
    }

    private class MnassaImageHolder(private val firebaseImage: String?) : ImageHolder(firebaseImage
            ?: "") {

        override fun applyTo(imageView: ImageView, tag: String?): Boolean {
            imageView.avatarRound(firebaseImage)

            return true
        }
    }

}
