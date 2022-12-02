package com.cesar.notasytareas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.cesar.notasytareas.data.NoteDatabase
import com.cesar.notasytareas.model.Multimedia
import java.text.SimpleDateFormat
import java.util.*

class MultimediaAdapter (var multimedia: List<Multimedia>): RecyclerView.Adapter<MultimediaAdapter.ViewHolder>(){

    class ViewHolder(v : View) : RecyclerView.ViewHolder(v){
        var idNota : TextView
        var foto : ImageView
        var btnDelete: ImageView
        var description: TextView
        var cardview: CardView


        init{
            idNota = v.findViewById(R.id.lblIdNTMultimedia)
            foto = v.findViewById(R.id.imgFotoCarga)
            btnDelete = v.findViewById(R.id.btnDeleteMultimedia)
            description = v.findViewById(R.id.lblDescripcion)
            cardview = v.findViewById(R.id.cardView)

        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_multimedia, parent, false)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = multimedia[position]
        holder.idNota.text = p.idNota.toString()
        if(p.type == 1){
            holder.foto.setImageURI(p.path.toUri())
        }else if(p.type == 2){
            holder.foto.setImageResource(R.drawable.videomultimedia)
        }else if(p.type == 3){
            holder.foto.setImageResource(R.drawable.audiomultimedia)
        }
        holder.description.text=p.description



        holder.cardview.setOnClickListener{ view : View ->
            var bundle = Bundle()
            bundle.putString("path", p.path)
            bundle.putString("description",p.description)
            bundle.putString("id",p.id.toString())
            bundle.putString("idNota",p.idNota.toString())

            //Navegar al fragmento donde se carga la imagen
            if(p.type == 1){
                view.findNavController().navigate(R.id.action_listFotos_to_foto,bundle)
            }else if(p.type == 2){
                view.findNavController().navigate(R.id.action_listFotos_to_video,bundle)
            }else if(p.type == 3){
                view.findNavController().navigate(R.id.action_listFotos_to_addAudio,bundle)
            }

        }

        holder.btnDelete.setOnClickListener{view : View ->
            NoteDatabase.getDatabase(holder.idNota.context).multimediaDao().deleteMultimedia(p)
            var multimedia  = NoteDatabase.getDatabase(holder.idNota.context).multimediaDao().getMultimedia(p.idNota)
            this.multimedia = multimedia
            this.notifyItemRemoved(position)
        }

    }


    override fun getItemCount(): Int {
        return multimedia.size
    }

}