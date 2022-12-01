package com.cesar.notasytareas

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
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
import com.cesar.notasytareas.databinding.FragmentFotoBinding
import java.util.*
import com.cesar.notasytareas.model.Multimedia
import com.cesar.notasytareas.model.Note
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat

class Foto : Fragment() {
    lateinit var miContexto:Context
    lateinit var photoURI: Uri
    lateinit var binding : FragmentFotoBinding
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
            Toast.makeText(context, "Hay foto", Toast.LENGTH_SHORT).show()

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
            Toast.makeText(context, "No hay foto :3", Toast.LENGTH_SHORT).show()

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == AppCompatActivity.RESULT_OK) {
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
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun setPic() {
        // Get the dimensions of the View
        val targetH: Int = binding.imgFotoTomada.width
        val targetW: Int = binding.imgFotoTomada.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.min( photoH / targetH,photoW / targetW)

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            binding.imgFotoTomada.setImageBitmap(bitmap)
        }
    }
}