package com.cesar.notasytareas.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note (
    @PrimaryKey(autoGenerate = true)
    val id : Int,
    val title: String,
    val description: String,
    val type: Boolean,
    val date: String,
    val completed : Boolean
){
    constructor(title: String, description: String, type: Boolean,date: String, completed: Boolean) :
            this(0,
                title,
                description,
                type,
                date,
                completed)
}