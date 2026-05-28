package com.questlog.core.data.privacy

import com.questlog.core.data.repository.ConsentRepositoryImpl
import com.questlog.core.domain.repository.ConsentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PrivacyModule {

    @Binds
    @Singleton
    abstract fun bindConsentRepository(impl: ConsentRepositoryImpl): ConsentRepository
}
