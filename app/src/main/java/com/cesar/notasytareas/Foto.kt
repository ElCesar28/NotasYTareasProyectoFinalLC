package com.cesar.notasytareas

import android.R.attr
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.cesar.notasytareas.data.NoteDatabase
import com.cesar.notasytareas.databinding.FragmentFotoBinding
import com.cesar.notasytareas.model.Multimedia
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class Foto : Fragment() {
    lateinit var miContexto: Context
    var photoURI: Uri="".toUri()
    lateinit var binding: FragmentFotoBinding
    lateinit var currentPhotoPath: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        miContexto=context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val bundle = Bundle()
        Intent.FLAG_GRANT_READ_URI_PERMISSION
        //Inicializamos el bindig e inflamos el fragmento
        binding = FragmentFotoBinding.inflate(layoutInflater)

        binding.btnTomarFoto.setOnClickListener{
            dispatchTakePictureIntent()
        }

        currentPhotoPath=""

        if(arguments?.getString("id")== null){
            binding.btnGuardar.visibility=View.INVISIBLE
        }

        if(arguments?.getString("path")!= null){
            binding.imgFotoTomada.setImageURI(arguments?.getString("path")!!.toUri())
            binding.descriptionMultimedia.setText(arguments?.getString("description"))
            binding.btnTomarFoto.visibility=View.INVISIBLE
            binding.btnGaleriaFoto.visibility=View.INVISIBLE

            bundle.putString("idNota",arguments?.getString("idNota"))
            bundle.putString("type","1")
            bundle.putString("path",arguments?.getString("path"))
            bundle.putString("description",binding.descriptionMultimedia.text.toString())

            binding.btnGuardar.setOnClickListener {
                lifecycleScope.launch{
                    NoteDatabase.getDatabase(requireActivity().applicationContext).multimediaDao()
                        .updateMultimedia(arguments?.getString("path").toString(),
                                          binding.descriptionMultimedia.text.toString(),
                                          arguments?.getString("id")!!.toInt())
                    it.findNavController().navigate(R.id.action_foto_to_listFotos,bundle)
                }
            }
        }else{

            binding.btnGuardar.setOnClickListener {
                bundle.putString("idNota",arguments?.getString("idNota"))
                bundle.putString("type","1")
                bundle.putString("path",photoURI.toString())
                bundle.putString("description",binding.descriptionMultimedia.text.toString())

                lifecycleScope.launch {
                    val foto = Multimedia( arguments?.getString("idNota")!!.toInt(),
                        1,
                        photoURI.toString(),
                        binding.descriptionMultimedia.text.toString())
                    NoteDatabase.getDatabase(requireActivity().applicationContext).multimediaDao().insert(foto)
                    it.findNavController().navigate(R.id.action_foto_to_listFotos,bundle)
                }
            }
        }

        binding.btnGaleriaFoto.setOnClickListener {
            val intent: Intent
            if (Build.VERSION.SDK_INT < 19) {
                intent = Intent()
                intent.setAction(Intent.ACTION_GET_CONTENT)
                intent.setType("image/*")
                startActivityForResult(intent, 111)
            } else {
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.setType("image/*")
                startActivityForResult(intent, 111)
            }
        }


        return binding.root
    }
    val REQUEST_TAKE_PHOTO = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
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
                        photoURI= FileProvider.getUriForFile(
                            this.miContexto,
                            "com.cesar.notasytareas.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == AppCompatActivity.RESULT_OK) {
            binding.imgFotoTomada.setImageURI(photoURI)
            binding.btnGuardar.visibility=View.VISIBLE
        }else if (requestCode == 111 && resultCode == Activity.RESULT_OK) {

            miContexto.grantUriPermission(miContexto.packageName,photoURI,Intent.FLAG_GRANT_READ_URI_PERMISSION)
            data?.data?.also { uri ->

                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                photoURI=uri
                // Check for the freshest data.
                miContexto.contentResolver.takePersistableUriPermission(photoURI, takeFlags)
                // you can get uri and perform any function on it
                // Perform operations on the document using its URI.
            }


            binding.imgFotoTomada.setImageURI(photoURI)
            binding.btnGuardar.visibility=View.VISIBLE
        }
    }



    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = activity?.getExternalFilesDir(null)
        return File.createTempFile(
            "foto_${timeStamp}_", /* prefix */
            ".png", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }


}