package com.cesar.notasytareas

import android.os.Binder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.ToggleButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cesar.notasytareas.data.NoteDatabase
import com.cesar.notasytareas.databinding.FragmentCreateNoteBinding
import com.cesar.notasytareas.databinding.FragmentFotoBinding
import com.cesar.notasytareas.databinding.FragmentListNoteBinding
import com.cesar.notasytareas.model.Note
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class ListNote : Fragment() {

    lateinit var notes : List<Note>
    lateinit var binding : FragmentListNoteBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_list_note, container, false)
        val rv = root.findViewById<RecyclerView>(R.id.listNotes)

        //view note
        lifecycleScope.launch {
            notes = NoteDatabase.getDatabase(requireActivity().applicationContext).noteDao()
                .getAllNotes()
        }
        root.findViewById<SearchView>(R.id.search).setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(text: String?): Boolean {
                var tempArr = ArrayList<Note>()
                for(arr in notes){
                    if(arr.title!!.toLowerCase(Locale.getDefault()).contains(text.toString())){
                        tempArr.add(arr)
                    }
                }

                rv.adapter=NoteAdapter(tempArr)
                rv.adapter!!.notifyDataSetChanged()
                NoteAdapter(tempArr).notifyDataSetChanged()
                return true
            }
        })



        rv.adapter = NoteAdapter(notes)
        rv.layoutManager = LinearLayoutManager(this@ListNote.requireContext())

        //Navigation
        root.findViewById<FloatingActionButton>(R.id.add).setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_listNote_to_createNote)
        }

        return root.rootView
    }
}