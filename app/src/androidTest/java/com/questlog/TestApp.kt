package com.questlog

import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.hilt.android.testing.HiltTestApplication

class TestApp : HiltTestApplication(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    override fun onCreate() {
        super.onCreate()
        WorkManagerTestInitHelper.initializeTestWorkManager(this, workManagerConfiguration)
    }
}
