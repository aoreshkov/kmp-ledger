package app.oreshkov.kmpledger.core.database.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import app.oreshkov.kmpledger.core.database.model.PostingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostingDao {
    @Insert
    suspend fun insert(posting: PostingEntity)

    @Delete
    suspend fun delete(posting: PostingEntity)

    @Update
    suspend fun update(posting: PostingEntity)

    @Query("SELECT * FROM postings")
    fun getAllPostings(): Flow<List<PostingEntity>>

    @Query("SELECT * FROM postings WHERE id = :id")
    fun getPostingById(id: Long): Flow<PostingEntity?>
}