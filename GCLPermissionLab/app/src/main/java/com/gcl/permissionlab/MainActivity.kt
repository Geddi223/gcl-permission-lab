package com.gcl.permissionlab

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var cameraPreview: PreviewView
    private lateinit var cameraButton: Button
    private lateinit var microphoneButton: Button
    private lateinit var playbackButton: Button
    private lateinit var locationButton: Button
    private lateinit var statusView: TextView

    private var cameraActive = false
    private var cameraUseCase: Preview? = null
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var isRecording = false
    private var recordingFile: File? = null

    private val cameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera() else setStatus("Camera: permission denied")
        }

    private val microphonePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startRecording() else setStatus("Microphone: permission denied")
        }

    private val locationPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            if (granted.values.any { it }) requestLocation()
            else setStatus("Location: permission denied")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(24))
            setBackgroundColor(0xFFF3F6FA.toInt())
        }

        page.addView(TextView(this).apply {
            text = "GCL CYBERSECURITY AWARENESS"
            textSize = 25f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(0xFF10243A.toInt())
        })
        page.addView(TextView(this).apply {
            text = "Authorized camera, microphone & location demo"
            textSize = 15f
            setTextColor(0xFF526579.toInt())
            setPadding(0, dp(6), 0, dp(18))
        })
        page.addView(TextView(this).apply {
            text = "DEMONSTRATION SAFETY\nAll access is visible and starts only after you press the matching control. " +
                "This app performs no background capture, hidden recording, or network transmission."
            textSize = 14f
            setTextColor(0xFF17435E.toInt())
            setPadding(dp(14), dp(14), dp(14), dp(14))
            setBackgroundColor(0xFFDCEEF8.toInt())
        })

        page.addSectionTitle("Camera")
        cameraPreview = PreviewView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(240)
            )
            setBackgroundColor(0xFF10243A.toInt())
        }
        page.addView(cameraPreview)
        cameraButton = page.addActionButton("Start camera") { toggleCamera() }

        page.addSectionTitle("Microphone")
        microphoneButton = page.addActionButton("Start recording") { toggleRecording() }
        playbackButton = page.addActionButton("Play recording") { playRecording() }.apply {
            isEnabled = false
        }

        page.addSectionTitle("Location")
        locationButton = page.addActionButton("Get current location") { getLocation() }

        statusView = TextView(this).apply {
            text = "Camera: stopped\nMicrophone: not recording\nLocation: not requested"
            textSize = 14f
            setTextColor(0xFF26384A.toInt())
            setPadding(dp(14), dp(14), dp(14), dp(14))
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        page.addView(statusView.apply {
            setPadding(dp(14), dp(14), dp(14), dp(14))
        })

        page.addView(TextView(this).apply {
            text = "\nFor this presentation, pair the app with phone settings and ADB only on your own authorized device."
            textSize = 12f
            setTextColor(0xFF718096.toInt())
            gravity = Gravity.CENTER
        })

        val scroll = ScrollView(this).apply { addView(page) }
        setContentView(scroll)
    }

    private fun toggleCamera() {
        if (cameraActive) stopCamera() else {
            if (hasPermission(Manifest.permission.CAMERA)) startCamera()
            else cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = cameraPreview.surfaceProvider
            }
            try {
                provider.unbindAll()
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview)
                cameraUseCase = preview
                cameraActive = true
                cameraButton.text = "Stop camera"
                setStatus("Camera: live preview started")
            } catch (error: Exception) {
                setStatus("Camera: unable to start (${error.message})")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopCamera() {
        val provider = ProcessCameraProvider.getInstance(this)
        provider.get()?.unbindAll()
        cameraActive = false
        cameraUseCase = null
        cameraButton.text = "Start camera"
        setStatus("Camera: stopped")
    }

    private fun toggleRecording() {
        if (isRecording) stopRecording()
        else if (hasPermission(Manifest.permission.RECORD_AUDIO)) startRecording()
        else microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun startRecording() {
        stopPlayback()
        recordingFile = File(filesDir, "gcl-recording.m4a")
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(recordingFile!!.absolutePath)
            try {
                prepare()
                start()
                isRecording = true
                microphoneButton.text = "Stop recording"
                playbackButton.isEnabled = false
                setStatus("Microphone: recording...")
            } catch (error: Exception) {
                setStatus("Microphone: unable to record (${error.message})")
                release()
                recorder = null
            }
        }
    }

    private fun stopRecording() {
        try {
            recorder?.stop()
        } catch (_: Exception) {
        } finally {
            recorder?.release()
            recorder = null
        }
        isRecording = false
        microphoneButton.text = "Start recording"
        playbackButton.isEnabled = recordingFile?.exists() == true
        setStatus("Microphone: saved locally (${recordingFile?.name})")
    }

    private fun playRecording() {
        if (player?.isPlaying == true) {
            stopPlayback()
            return
        }
        val file = recordingFile
        if (file == null || !file.exists()) {
            setStatus("Microphone: no recording available")
            return
        }
        stopPlayback()
        player = MediaPlayer().apply {
            try {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    playbackButton.text = "Play recording"
                    release()
                    player = null
                }
                start()
                playbackButton.text = "Stop playback"
            } catch (error: Exception) {
                setStatus("Microphone: unable to play (${error.message})")
                release()
                player = null
            }
        }
    }

    private fun stopPlayback() {
        if (player?.isPlaying == true) player?.stop()
        player?.release()
        player = null
        playbackButton.text = "Play recording"
    }

    private fun getLocation() {
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) requestLocation()
        else locationPermission.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        setStatus("Location: requesting current position...")
        val client = LocationServices.getFusedLocationProviderClient(this)
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    setStatus(
                        "Location:\nLatitude: ${"%.6f".format(location.latitude)}\n" +
                            "Longitude: ${"%.6f".format(location.longitude)}\n" +
                            "Accuracy: ${"%.1f".format(location.accuracy)} m"
                    )
                } else {
                    setStatus("Location: no position was available")
                }
            }
            .addOnFailureListener { error ->
                setStatus("Location: unable to get position (${error.message})")
            }
    }

    override fun onStop() {
        if (isRecording) stopRecording()
        if (cameraActive) stopCamera()
        stopPlayback()
        super.onStop()
    }

    override fun onDestroy() {
        if (cameraActive) stopCamera()
        super.onDestroy()
    }

    private fun setStatus(text: String) {
        statusView.text = text
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun LinearLayout.addSectionTitle(text: String) {
        addView(TextView(this@MainActivity).apply {
            this.text = text
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(0xFF10243A.toInt())
            setPadding(0, dp(18), 0, dp(8))
        })
    }

    private fun LinearLayout.addActionButton(label: String, action: () -> Unit): Button =
        Button(this@MainActivity).apply {
            text = label
            isAllCaps = false
            setOnClickListener { action() }
        }.also { button ->
            this@addActionButton.addView(button, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) })
        }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
