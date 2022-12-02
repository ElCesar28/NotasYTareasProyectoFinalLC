package com.cesar.notasytareas

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.cesar.notasytareas.data.NoteDatabase
import com.cesar.notasytareas.model.Note
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter(var notes: List<Note>): RecyclerView.Adapter<NoteAdapter.ViewHolder>(){

    class ViewHolder(v : View) : RecyclerView.ViewHolder(v){
        var name : TextView
        var description : TextView
        var btn_delete: ImageView
        var btn_edit : ImageView
        var dateCompleted: TextView
        var item: CardView


        init{
            name = v.findViewById(R.id.lblTitulo)
            description = v.findViewById(R.id.lblContenido)
            btn_delete = v.findViewById(R.id.btnDelete)
            btn_edit = v.findViewById(R.id.btnEdit)
            dateCompleted = v.findViewById(R.id.lblFecha)
            item=v.findViewById(R.id.cardView)
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)

        return ViewHolder(v)
    }

    //Fecha actual
    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }
    fun getCurrentDateTime(): Date { return Calendar.getInstance().time }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = notes[position]
        holder.name.text = p.title.toString()
        holder.description.text =p.description.toString()
        if(p.type){
            holder.item.setCardBackgroundColor(Color.parseColor("#52009688"))
            if(p.completed){
                holder.dateCompleted.text="Completada"
                holder.dateCompleted.setTextColor(Color.parseColor("#FF4CAF50"))
            }else{
                holder.dateCompleted.text="Sin completar"
                holder.dateCompleted.setTextColor(Color.parseColor("#FFBC1408"))
            }

        }else{
            holder.item.setCardBackgroundColor(Color.parseColor("#45FFEB3B"))
            holder.dateCompleted.text=p.date.toString()
        }
        holder.btn_delete.setOnClickListener{view : View ->
            NoteDatabase.getDatabase(holder.name.context).noteDao().deleteNote(p)
            var notes  = NoteDatabase.getDatabase(holder.name.context).noteDao().getAllNotes()
            this.notes = notes
            this.notifyItemRemoved(position)
        }


        holder.item.setOnClickListener{ view : View ->
            var bundle = Bundle()
            bundle.putString("id",p.id.toString())
            bundle.putString("title", p.title)
            bundle.putString("description", p.description)
            bundle.putString("type", p.type.toString())
            bundle.putString("date",getCurrentDateTime().toString("dd/MM/yyyy HH:mm:ss"))
            bundle.putString("completed",p.completed.toString())
            view.findNavController().navigate(R.id.action_listNote_to_createNote,bundle)

        }

    }


    override fun getItemCount(): Int {
        return notes.size
    }

}