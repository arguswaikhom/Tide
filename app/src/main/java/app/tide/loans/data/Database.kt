package app.tide.loans.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM person")
    fun people(): Flow<List<Person>>

    @Query("SELECT * FROM entry")
    fun entries(): Flow<List<Entry>>

    @Query("SELECT * FROM settings WHERE id = 1")
    fun settings(): Flow<Settings?>

    @Insert
    suspend fun insert(person: Person): Long

    @Update
    suspend fun update(person: Person)

    @Query("DELETE FROM person WHERE id = :id")
    suspend fun deletePerson(id: Long)

    @Upsert
    suspend fun save(entry: Entry)

    @Query("DELETE FROM entry WHERE id = :id")
    suspend fun deleteEntry(id: Long)

    @Upsert
    suspend fun save(settings: Settings)
}

@Database(entities = [Person::class, Entry::class, Settings::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): LoanDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "tide.db")
                .build()
                .also { instance = it }
        }
    }
}
