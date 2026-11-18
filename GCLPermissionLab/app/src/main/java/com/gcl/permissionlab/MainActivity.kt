package com.gcl.permissionlab

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var statusView: TextView

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            updateStatus("Camera", granted)
        }

    private val microphoneLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            updateStatus("Microphone", granted)
        }

    private val locationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val approximate = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            val precise = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
            statusView.text = when {
                precise -> "Location: PRECISE permission granted"
                approximate -> "Location: APPROXIMATE permission granted"
                else -> "Location: permission not granted"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(24))
            setBackgroundColor(0xFFF3F6FA.toInt())
        }

        val scroll = ScrollView(this).apply {
            addView(page)
        }

        val title = TextView(this).apply {
            text = "GCL PERMISSION LAB"
            textSize = 25f
            setTextColor(0xFF10243A.toInt())
            typeface = Typeface.DEFAULT_BOLD
        }
        page.addView(title)

        val subtitle = TextView(this).apply {
            text = "Android privacy & permission demonstration"
            textSize = 15f
            setTextColor(0xFF526579.toInt())
            setPadding(0, dp(6), 0, dp(18))
        }
        page.addView(subtitle)

        val notice = TextView(this).apply {
            text = "TRAINING APP ONLY\nEach button opens Android's permission prompt. " +
                "This app does not take photos, record audio, read your location, " +
                "or send data anywhere."
            textSize = 14f
            setTextColor(0xFF17435E.toInt())
            setPadding(dp(14), dp(14), dp(14), dp(14))
            setBackgroundColor(0xFFDCEEF8.toInt())
        }
        page.addView(notice)

        addActionButton(page, "Test camera permission") {
            cameraLauncher.launch(Manifest.permission.CAMERA)
        }
        addActionButton(page, "Test microphone permission") {
            microphoneLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        addActionButton(page, "Test location permission") {
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }

        val statusTitle = TextView(this).apply {
            text = "Permission result"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF10243A.toInt())
            setPadding(0, dp(20), 0, dp(8))
        }
        page.addView(statusTitle)

        statusView = TextView(this).apply {
            text = "Camera: ${permissionState(Manifest.permission.CAMERA)}\n" +
                "Microphone: ${permissionState(Manifest.permission.RECORD_AUDIO)}\n" +
                "Location: ${locationState()}"
            textSize = 14f
            setTextColor(0xFF26384A.toInt())
            setPadding(dp(14), dp(14), dp(14), dp(14))
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        page.addView(statusView)

        val footer = TextView(this).apply {
            text = "\nGedi Creative Lab • Authorized educational use"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(0xFF718096.toInt())
        }
        page.addView(footer)

        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        if (::statusView.isInitialized) refreshStatus()
    }

    private fun addActionButton(parent: LinearLayout, label: String, action: () -> Unit) {
        val button = Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { action() }
        }
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(12)
        }
        parent.addView(button, params)
    }

    private fun updateStatus(name: String, granted: Boolean) {
        refreshStatus()
        statusView.append("\nLast request — $name: ${if (granted) "GRANTED" else "DENIED"}")
    }

    private fun refreshStatus() {
        statusView.text = "Camera: ${permissionState(Manifest.permission.CAMERA)}\n" +
            "Microphone: ${permissionState(Manifest.permission.RECORD_AUDIO)}\n" +
            "Location: ${locationState()}"
    }

    private fun permissionState(permission: String): String =
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED)
            "GRANTED" else "NOT GRANTED"

    private fun locationState(): String {
        val fine = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return when {
            fine -> "PRECISE GRANTED"
            coarse -> "APPROXIMATE GRANTED"
            else -> "NOT GRANTED"
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
