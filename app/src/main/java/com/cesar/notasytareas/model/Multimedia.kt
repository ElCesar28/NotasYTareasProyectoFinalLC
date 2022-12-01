package com.cesar.notasytareas.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Multimedia(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var idNota: Int,
    val type: Int,
    val path: String,
    val description: String
){
    constructor(idNota: Int, type: Int, path: String,description: String) :
            this(0,
                idNota,
                type,
                path,
                description)
}