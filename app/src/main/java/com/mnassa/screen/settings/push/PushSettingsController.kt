package com.mnassa.screen.settings.push

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.impl.PushSettingModelImpl
import com.mnassa.screen.base.MnassaControllerImpl
import kotlinx.android.synthetic.main.controller_push_settings.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/20/2018
 */
class PushSettingsController : MnassaControllerImpl<PushSettingsViewModel>() {
    override val layoutId: Int = R.layout.controller_push_settings

    override val viewModel: PushSettingsViewModel by instance()

    private val adapter = PushSettingAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view) {
            rvPushSettings.layoutManager = LinearLayoutManager(view.context)
            rvPushSettings.adapter = adapter
        }
        adapter.onSettingReceiveClick = { setting ->
            Timber.i(setting.toString())
            viewModel.changeSetting(PushSettingModelImpl(
                    isActive = !setting.isActive,
                    withSound = setting.withSound,
                    name = setting.name
            ))
        }
        adapter.onSettingVolumeClick = { setting ->
            Timber.i(setting.toString())
            viewModel.changeSetting(PushSettingModelImpl(
                    isActive = setting.isActive,
                    withSound = !setting.withSound,
                    name = setting.name
            ))
        }
        launchCoroutineUI {
            viewModel.notificationChangeChannel.consumeEach {
                Timber.i(it.toString())
                adapter.dataStorage.addAll(it)
            }
        }
        launchCoroutineUI {
            val settings = viewModel.getSettings()
            adapter.set(settings)
            adapter.notifyDataSetChanged()
        }
    }

    companion object {

        fun newInstance() = PushSettingsController()
    }

}