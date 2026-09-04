package com.zyb.studyflow

import android.app.Application
import com.zyb.studyflow.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class StudyFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@StudyFlowApplication)
            modules(appModule)
        }
    }
}
