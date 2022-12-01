package com.cesar.notasytareas.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.cesar.notasytareas.model.Multimedia

@Dao
interface MultimediaDao {
    @Insert
    fun insert(multimedia: Multimedia)

    @Query("SELECT * FROM Multimedia WHERE idNota = :idNota")
    fun getMultimedia(idNota: Int): List<Multimedia>

    @Query("UPDATE Multimedia set path = :path,description = :description WHERE id = :id")
    fun updateMultimedia(path: String, description: String, id: Int)

    @Delete
    fun deleteMultimedia(multimedia: Multimedia)
}