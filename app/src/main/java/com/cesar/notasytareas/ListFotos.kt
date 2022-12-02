package com.cesar.notasytareas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cesar.notasytareas.data.MultimediaDao
import com.cesar.notasytareas.data.NoteDatabase
import com.cesar.notasytareas.model.Multimedia
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ListFotos: Fragment() {

    lateinit var multimedia: List<Multimedia>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bundle = Bundle()
        val root = inflater.inflate(R.layout.fragment_list_fotos, container, false)
        val rv = root.findViewById<RecyclerView>(R.id.listfotos)

        bundle.putString("idNota", arguments?.getString("idNota"))

        //view note
        lifecycleScope.launch {
            multimedia = NoteDatabase.getDatabase(requireActivity().applicationContext).multimediaDao()
                .getMultimedia(arguments?.getString("idNota")!!.toInt())
        }

        rv.adapter = MultimediaAdapter(multimedia)
        rv.layoutManager = LinearLayoutManager(this@ListFotos.requireContext())

        //Navigation
        //Toast.makeText(context, arguments?.getString("idNota"), LENGTH_SHORT).show()
        root.findViewById<ImageView>(R.id.imgCam).setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_listFotos_to_foto,bundle)
        }
        root.findViewById<ImageView>(R.id.imgVideoCam).setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_listFotos_to_video,bundle)
        }
        root.findViewById<ImageView>(R.id.imgMicro).setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_listFotos_to_addAudio,bundle)
        }
        root.findViewById<ImageView>(R.id.imgHome).setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_listFotos_to_listNote,bundle)
        }
        return root.rootView
    }
}