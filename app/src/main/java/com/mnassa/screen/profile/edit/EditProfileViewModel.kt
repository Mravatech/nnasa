package com.mnassa.screen.profile.edit

import android.net.Uri
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.widget.ChipsAdapter
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/26/2018
 */
interface EditProfileViewModel : MnassaViewModel, ChipsAdapter.ChipSearch  {
    val imageUploadedChannel: BroadcastChannel<String>
    val tagChannel: BroadcastChannel<List<TagModel>>
    fun getTagsByIds(ids: List<String>)
    fun uploadPhotoToStorage(uri: Uri)
}