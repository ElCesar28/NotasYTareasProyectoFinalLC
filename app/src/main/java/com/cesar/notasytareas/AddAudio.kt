package com.cesar.notasytareas

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.cesar.notasytareas.data.NoteDatabase
import com.cesar.notasytareas.databinding.FragmentAddAudioBinding
import com.cesar.notasytareas.databinding.FragmentFotoBinding
import com.cesar.notasytareas.model.Multimedia
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val LOG_TAG = "AudioRecordTest"


class AddAudio : Fragment() {
    private var mStartRecording: Boolean = true
    //variables permisos
    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var fileName: String = ""
    lateinit var uri : Uri
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    lateinit var miContexto: Context
    lateinit var binding : FragmentAddAudioBinding
    private var audio:String=""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        miContexto=context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val bundle = Bundle()

        // Inflate the layout for this fragment
        binding = FragmentAddAudioBinding.inflate(layoutInflater)

        iniPerm()

        if(arguments?.getString("path")!= null){

            fileName= arguments?.getString("path")!!
            binding.addAudioTitle.setText(arguments?.getString("description"))
            binding.addAudioGrabar.visibility=View.INVISIBLE

            bundle.putString("idNota",arguments?.getString("idNota"))
            bundle.putString("type","3")
            bundle.putString("path",arguments?.getString("path"))
            bundle.putString("description",binding.addAudioTitle.text.toString())

            onPlay(mStartPlaying)
            if (fileName.length > 0) {
                when (mStartPlaying) {
                    true -> Toast.makeText(context, "Parando", Toast.LENGTH_SHORT).show()
                    false -> Toast.makeText(context, "Reproduciendo", Toast.LENGTH_SHORT).show()
                }
                mStartPlaying = !mStartPlaying
            } else {
                Toast.makeText(
                    context,
                    "Debes grabar un audio" + fileName,
                    Toast.LENGTH_SHORT
                ).show()
            }

            binding.addAudioGuardar.setOnClickListener {
                lifecycleScope.launch{
                    NoteDatabase.getDatabase(requireActivity().applicationContext).multimediaDao()
                        .updateMultimedia(arguments?.getString("path").toString(),
                            binding.addAudioTitle.text.toString(),
                            arguments?.getString("id")!!.toInt())
                    it.findNavController().navigate(R.id.action_addAudio_to_listFotos,bundle)
                }
            }
        }else{
            binding.addAudioGuardar.setOnClickListener {
                bundle.putString("idNota",arguments?.getString("idNota"))
                bundle.putString("type","3")
                bundle.putString("path",fileName)
                bundle.putString("description",binding.addAudioTitle.text.toString())

                if (fileName.length > 0) {
                    lifecycleScope.launch {
                        val audio = Multimedia( arguments?.getString("idNota")!!.toInt(),
                            3,
                            fileName,
                            binding.addAudioTitle.text.toString())
                        NoteDatabase.getDatabase(requireActivity().applicationContext).multimediaDao().insert(audio)
                        it.findNavController().navigate(R.id.action_addAudio_to_listFotos,bundle)
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Debes grabar un audio" + fileName,
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

        }

        binding.addAudioGrabar.setOnClickListener {
            grabar()
            grabando(mStartRecording)
            binding.addAudioGrabar.text = when (mStartRecording) {
                true -> "Parar"
                false -> "Grabar"
            }

            if (mStartRecording) {
                Toast.makeText(context, "Grabando", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Archivo en ", Toast.LENGTH_SHORT).show()
            }
            mStartRecording = !mStartRecording
        }

        binding.addAudioImage.setOnClickListener {
            onPlay(mStartPlaying)
            if (fileName.length > 0) {
                when (mStartPlaying) {
                    true -> Toast.makeText(context, "Parando", Toast.LENGTH_SHORT).show()
                    false -> Toast.makeText(context, "Reproduciendo", Toast.LENGTH_SHORT).show()
                }
                mStartPlaying = !mStartPlaying
            } else {
                Toast.makeText(
                    context,
                    "Debes grabar un audio" + fileName,
                    Toast.LENGTH_SHORT
                ).show()
            }

        }


        //binding.addAudioSeleccionar.setOnClickListener {
        //    val intent = Intent()
        //        .setType("audio/*")
        //        .setAction(Intent.ACTION_GET_CONTENT)
        //    startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
        //}
        return binding.root


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            fileName = data?.data.toString() // The URI with the location of the file
        }
    }
    //permisos
    private fun grabar() {
        revisarPermisos()
    }

    private fun iniPerm() {
        // Register the permissions callback, which handles the user's response to the
        // system permissions dialog. Save the return value, an instance of
        // ActivityResultLauncher. You can use either a val, as shown in this snippet,
        // or a lateinit var in your onAttach() or onCreate() method.
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }

    }

    private fun revisarPermisos() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                "android.permission.RECORD_AUDIO"
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.

            }
            shouldShowRequestPermissionRationale("android.permission.RECORD_AUDIO") -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                //showInContextUI(...)
                //Toast.makeText(applicationContext, "Debes dar perimso para grabar audios", Toast.LENGTH_SHORT).show()
                MaterialAlertDialogBuilder(
                    requireContext()
                )
                    .setTitle("Title")
                    .setMessage("Debes dar perimso para grabar audios")
                    .setNegativeButton("Cancel") { dialog, which ->
                    }
                    .setPositiveButton("OK") { dialog, which ->
                        requestPermissions(
                            arrayOf(
                                "android.permission.RECORD_AUDIO",
                                "android.permission.WRITE_EXTERNAL_STORAGE",
                                "android.permission.READ_MEDIA_AUDIO"
                            ),
                            1001
                        )

                    }
                    .show()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                /*requestPermissionLauncher.launch(
                    "android.permission.RECORD_AUDIO")*/
                requestPermissions(
                    arrayOf(
                        "android.permission.RECORD_AUDIO",
                        "android.permission.WRITE_EXTERNAL_STORAGE",
                        "android.permission.READ_MEDIA_AUDIO"
                    ),
                    1001
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1001 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                            grantResults[1] == PackageManager.PERMISSION_GRANTED
                            )
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    iniciarGrabacion()
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't l
                    // ink to
                    // system settings in an effort to convince the user to change
                    // their decision.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    //grabacion
    private fun grabando(start: Boolean) = if (start) {
        iniciarGrabacion()
    } else {
        pararGrabacion()
    }


    private fun iniciarGrabacion() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            createAudioFile()
            //////////esto es lo importante para reproducir de nuevo el audio
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "" + e.message)
            }

            start()
        }
    }


    private fun pararGrabacion() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    @Throws(IOException::class)
    private fun createAudioFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        //val storageDir: File? =
        //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val storageDir: File? = requireActivity().filesDir
        return File.createTempFile(
            "Audio_${timeStamp}_", /* prefix */
            ".mp3", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            fileName = absolutePath
        }
    }

    //Player
    private fun onPlay(start: Boolean) = if (start) {
        startPlaying()
    } else {
        stopPlaying()
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {

                uri=Uri.parse(fileName)
                setDataSource(uri.path)
                prepare()
                start()

            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }
    var mStartPlaying = true

}