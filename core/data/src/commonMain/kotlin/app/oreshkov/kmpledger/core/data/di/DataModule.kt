package app.oreshkov.kmpledger.core.data.di

import app.oreshkov.kmpledger.core.data.repository.PostingRepository
import app.oreshkov.kmpledger.core.data.repository.OfflineFirstPostingRepository
import app.oreshkov.kmpledger.core.database.dao.PostingDao
import app.oreshkov.kmpledger.core.database.di.DatabaseModule
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [DatabaseModule::class])
class DataModule {
     @Single
     fun providePostingRepository(postingDao: PostingDao): PostingRepository = OfflineFirstPostingRepository(postingDao)
}