package com.example.sftping.di

import com.example.sftping.security.InMemoryKnownHostsStore
import com.example.sftping.security.KnownHostsStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {

    @Binds
    @Singleton
    abstract fun bindKnownHostsStore(impl: InMemoryKnownHostsStore): KnownHostsStore
}
