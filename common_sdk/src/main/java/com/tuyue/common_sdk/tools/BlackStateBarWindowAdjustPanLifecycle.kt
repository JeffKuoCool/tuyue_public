package com.tuyue.common_sdk.tools

import BaseActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.gyf.immersionbar.ImmersionBar

class BlackStateBarWindowAdjustPanLifecycle(private var activity: BaseActivity?) :
    LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        activity?.let {
            ImmersionBar.with(it)
                .transparentStatusBar()
                .statusBarDarkFont(true)
                .init()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        activity = null
    }
}