package com.questlog.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.questlog.core.data.di.AppModule
import com.questlog.core.domain.usecase.ResolveCombatUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

private val Context.testDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "questlog_test_prefs")

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class],
)
object TestAppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        val store = context.testDataStore
        runBlocking {
            store.edit { prefs ->
                prefs[booleanPreferencesKey("onboarding_completed")] = true
            }
        }
        return store
    }

    @Provides
    fun provideResolveCombatUseCase(): ResolveCombatUseCase = ResolveCombatUseCase()
}
