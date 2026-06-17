package com.questlog.core.data.di

import android.content.Context
import com.questlog.core.data.GoogleAuthManager
import com.questlog.core.data.db.QuestLogDatabase
import com.questlog.core.data.repository.DriveBackupRepositoryImpl
import com.questlog.core.domain.repository.DriveBackupRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DriveOkHttp

@Module
@InstallIn(SingletonComponent::class)
object DriveModule {

    // Anthropic 인터셉터 없는 전용 클라이언트 — Drive REST API 전용
    @Provides
    @Singleton
    @DriveOkHttp
    fun provideDriveOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideDriveBackupRepository(
        @ApplicationContext context: Context,
        authManager: GoogleAuthManager,
        database: QuestLogDatabase,
        @DriveOkHttp okHttpClient: OkHttpClient,
    ): DriveBackupRepository = DriveBackupRepositoryImpl(context, authManager, database, okHttpClient)
}
