package com.example.sftping.di

import com.example.sftping.sftp.ISftpClient
import com.example.sftping.sftp.JschSftpClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SftpModule {

    @Binds
    @Singleton
    abstract fun bindSftpClient(impl: JschSftpClient): ISftpClient
}
