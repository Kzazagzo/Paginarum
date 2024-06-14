package put.paginarum.database.al

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AlDao {
    @Query("SELECT * FROM alTrackers WHERE providerNovelName=:providerNovelName")
    suspend fun getAlTrackerByProviderNovelName(providerNovelName: String): AlTrackerEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlTracker(alTrackerEntity: AlTrackerEntity)

    @Delete
    suspend fun deleteAlTracker(alTrackerEntity: AlTrackerEntity)

    @Query("DELETE FROM alTrackers")
    suspend fun deleteAllTrackers()
}
