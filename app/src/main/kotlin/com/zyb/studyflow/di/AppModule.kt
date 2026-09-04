package com.zyb.studyflow.di

import androidx.room.Room
import com.zyb.studyflow.data.local.StudyFlowDatabase
import com.zyb.studyflow.data.repository.AssetTextbookRepository
import com.zyb.studyflow.data.repository.OfflineStudyRepository
import com.zyb.studyflow.data.repository.StudyRepository
import com.zyb.studyflow.data.repository.TextbookRepository
import com.zyb.studyflow.ui.StudyViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            StudyFlowDatabase::class.java,
            "studyflow.db",
        ).build()
    }
    single { get<StudyFlowDatabase>().studyProgressDao() }
    single<TextbookRepository> { AssetTextbookRepository(androidContext()) }
    single<StudyRepository> {
        OfflineStudyRepository(
            textbookRepository = get(),
            progressDao = get(),
        )
    }
    viewModelOf(::StudyViewModel)
}
