package app.oreshkov.ledger.core.data.di

import app.oreshkov.ledger.core.data.repository.OfflineFirstPostingRepository
import app.oreshkov.ledger.core.domain.di.DomainModule
import app.oreshkov.ledger.core.domain.repository.PostingRepository
import app.oreshkov.ledger.core.database.dao.PostingDao
import app.oreshkov.ledger.core.database.di.DatabaseModule
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [DatabaseModule::class, DomainModule::class])
class DataModule {
     @Single
     fun providePostingRepository(postingDao: PostingDao): PostingRepository = OfflineFirstPostingRepository(postingDao)
}