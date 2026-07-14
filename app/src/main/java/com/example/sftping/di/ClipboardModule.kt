package com.example.sftping.di

import com.example.sftping.util.AndroidClipboard
import com.example.sftping.util.Clipboard
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ClipboardModule {

    @Binds
    @Singleton
    abstract fun bindClipboard(impl: AndroidClipboard): Clipboard
}
