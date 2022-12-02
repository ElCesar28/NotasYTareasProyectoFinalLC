package com.cesar.notasytareas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cesar.notasytareas.model.Multimedia
import com.cesar.notasytareas.model.Note
import com.cesar.notasytareas.model.Recordatorio

@Database(entities = [Note::class, Multimedia::class, Recordatorio::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase(){
    abstract fun noteDao(): NoteDao
    abstract fun multimediaDao(): MultimediaDao
    abstract fun recordatorioDao(): RecordatorioDao
    companion object {
        private var INSTANCE: NoteDatabase? = null
        fun getDatabase(context: Context): NoteDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE =
                        Room.databaseBuilder(context,NoteDatabase::class.java, "note")
                            .allowMainThreadQueries()
                            .build()
                }
            }
            return INSTANCE!!
        }
    }

}