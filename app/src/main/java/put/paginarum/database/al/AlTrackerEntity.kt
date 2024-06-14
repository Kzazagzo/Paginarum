package put.paginarum.database.al

import androidx.room.Entity
import androidx.room.PrimaryKey
import put.paginarum.data.repository.network.AniListRepository

@Entity(tableName = "alTrackers")
class AlTrackerEntity(
    @PrimaryKey val alId: String,
    val alNovelName: String,
    val providerNovelName: String,
    var selectedTracking: AniListRepository.AlNovelStatus?,
)
