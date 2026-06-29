package com.example.sftping.di

import com.example.sftping.security.DataStoreKnownHostsStore
import com.example.sftping.security.KnownHostsStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.security.KeyStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {

    @Binds
    @Singleton
    abstract fun bindKnownHostsStore(impl: DataStoreKnownHostsStore): KnownHostsStore

    companion object {
        @Provides
        @Singleton
        fun provideKeyStore(): KeyStore =
            KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    }
}
