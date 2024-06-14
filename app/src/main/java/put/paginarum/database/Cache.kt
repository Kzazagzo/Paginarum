package put.paginarum.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

// :>>>>>
private class CacheConverter {
    @TypeConverter
    fun toDocument(value: String): Document {
        return Jsoup.parse(value)
    }

    @TypeConverter
    fun fromDocument(value: Document): String {
        return value.outerHtml()
    }
}

@TypeConverters(CacheConverter::class) // ten raczej tak, a ten nwm no ale klucz główny...
@Entity(tableName = "cache", indices = [Index(value = ["timestamp"]), Index(value = ["url"])])
class CachedPage(
    @PrimaryKey val url: String,
    val html: Document,
    val timestamp: Long = System.currentTimeMillis(),
) {
    object CacheConverter {
        @TypeConverter
        fun fromHtml(html: String): Document {
            return Jsoup.parse(html)
        }

        @TypeConverter
        fun documentToHtml(document: Document): String {
            return document.html()
        }
    }
}

@Dao
interface CacheDao {
    @Query("SELECT html FROM cache WHERE url=:url")
    suspend fun getCachedPage(url: String): Document?

    // 50 będzie i eee może nie będzie aż tak wolne
    @Query("DELETE FROM cache WHERE url NOT IN (SELECT url FROM cache ORDER BY timestamp DESC LIMIT 50)")
    suspend fun deleteExcessEntries()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedPage(cachedPage: CachedPage)

    @Transaction
    suspend fun htmlSet(cachedPage: CachedPage) {
        insertCachedPage(cachedPage)
        deleteExcessEntries()
    }
}
