package com.cesar.notasytareas

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.cesar.notasytareas.data.NoteDatabase
import com.cesar.notasytareas.databinding.FragmentVideoBinding
import com.cesar.notasytareas.model.Multimedia
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Video : Fragment() {
    private val REQUEST_TAKE_VIDEO: Int = 1001
    lateinit var miContexto: Context
    lateinit var photoURI: Uri
    lateinit var binding : FragmentVideoBinding
    lateinit var currentPhotoPath: String
    lateinit var mediaController: MediaController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        miContexto=context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val bundle = Bundle()

        //Inicializamos el bindig e inflamos el fragmento
        binding = FragmentVideoBinding.inflate(layoutInflater)

        binding.btnTomarVideo.setOnClickListener{
                tomarVideo()
        }

        if(arguments?.getString("id")== null){
            binding.btnGuardarVideo.visibility=View.INVISIBLE
        }

        if(arguments?.getString("path")!= null){
            Toast.makeText(context, "Hay video", Toast.LENGTH_SHORT).show()

            binding.videoView.setVideoURI(arguments?.getString("path")!!.toUri())
            binding.videoView.start()
            binding.videoView.setOnClickListener { binding.videoView.start() }

            binding.descriptionMultimediaVideo.setText(arguments?.getString("description"))
            binding.btnTomarVideo.visibility= View.INVISIBLE
            binding.btnGaleriaVideo.visibility= View.INVISIBLE

            bundle.putString("idNota",arguments?.getString("idNota"))
            bundle.putString("type","2")
            bundle.putString("path",arguments?.getString("path"))
            bundle.putString("description",binding.descriptionMultimediaVideo.text.toString())

            binding.btnGuardarVideo.setOnClickListener {
                lifecycleScope.launch{
                    NoteDatabase.getDatabase(requireActivity().applicationContext).multimediaDao()
                        .updateMultimedia(arguments?.getString("path").toString(),
                            binding.descriptionMultimediaVideo.text.toString(),
                            arguments?.getString("id")!!.toInt())
                    it.findNavController().navigate(R.id.action_video_to_listFotos,bundle)
                }
            }
        }else{
            //Toast.makeText(context, "No hay foto :3", Toast.LENGTH_SHORT).show()

            binding.btnGuardarVideo.setOnClickListener {
                bundle.putString("idNota",arguments?.getString("idNota"))
                bundle.putString("type","2")
                bundle.putString("path",photoURI.toString())
                bundle.putString("description",binding.descriptionMultimediaVideo.text.toString())

                lifecycleScope.launch {
                    val video = Multimedia( arguments?.getString("idNota")!!.toInt(),
                        2,
                        photoURI.toString(),
                        binding.descriptionMultimediaVideo.text.toString())
                    NoteDatabase.getDatabase(requireActivity().applicationContext).multimediaDao().insert(video)
                    it.findNavController().navigate(R.id.action_video_to_listFotos,bundle)
                }
            }
        }
        return binding.root
    }

    private fun tomarVideo() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            activity?.let {
                takePictureIntent.resolveActivity(it.packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File

                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        photoURI = FileProvider.getUriForFile(
                            this.miContexto,
                            "com.cesar.notasytareas.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_VIDEO)
                    }
                }
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_VIDEO && resultCode == AppCompatActivity.RESULT_OK) {
            binding.videoView.setVideoURI(photoURI)
            binding.videoView.start()
            binding.videoView.setOnClickListener { binding.videoView.start() }
            binding.btnGuardarVideo.visibility=View.VISIBLE
        }
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = activity?.getExternalFilesDir(null)
        return File.createTempFile(
            "Video_${timeStamp}_", /* prefix */
            ".mp4", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }


}