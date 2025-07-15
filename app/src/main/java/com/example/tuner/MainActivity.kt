package com.example.tuner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
class MainActivity : AppCompatActivity(), SettingsDialog.SettingsListener {

    private lateinit var tvNote: TextView
    private lateinit var tvCents: TextView
    private lateinit var needleView: CalibrationNeedleView
    private lateinit var waveView: AudioWaveView
    private lateinit var btnDiapason: Button
    private lateinit var btnSettings: Button
    private lateinit var btnRegister: Button
    private lateinit var btnTonalitaSalvate: Button


    private lateinit var btnIncreaseFreq: Button
    private lateinit var btnDecreaseFreq: Button
    private lateinit var tvReferenceFreq: TextView

    private var isPlaying = false
    private var audioTrack: AudioTrack? = null

    private var currentNote = "A"
    private var currentOctave = 4


    private var referenceFrequency = 440.0

    private val RECORD_AUDIO_REQUEST_CODE = 123

    private var dispatcherThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        loadSettings()
        startTunerAutomatically()
    }

    private fun initViews() {
        tvNote = findViewById(R.id.tvNote)
        tvCents = findViewById(R.id.tvCents)
        needleView = findViewById(R.id.needleView)
        waveView = findViewById(R.id.waveView)
        btnDiapason = findViewById(R.id.btnDiapason)
        btnSettings = findViewById(R.id.btnSettings)
        btnRegister= findViewById(R.id.btnRegister)
        btnTonalitaSalvate=findViewById(R.id.btnTonalitaSalvate)
        btnIncreaseFreq = findViewById(R.id.btnIncreaseFreq)
        btnDecreaseFreq = findViewById(R.id.btnDecreaseFreq)
        tvReferenceFreq = findViewById(R.id.tvReferenceFreq)
        btnDiapason.setOnClickListener { playDiapasonNote(currentNote, currentOctave) }
        btnSettings.setOnClickListener { showSettingsDialog() }
        btnIncreaseFreq.setOnClickListener {
            increaseReferenceFrequency()
            updateReferenceFreqUI()
        }
        btnDecreaseFreq.setOnClickListener {
            decreaseReferenceFrequency()
            updateReferenceFreqUI()
        }
        btnRegister.setOnClickListener {
            val intent = Intent(this, TonalitaActivity::class.java)
            startActivity(intent)
        }
        btnTonalitaSalvate.setOnClickListener {
            val intent = Intent(this, TonalitaSalvateActivity::class.java)
            startActivity(intent) }

        updateReferenceFreqUI()
    }

    private fun loadSettings() {
        val sharedPrefs = getSharedPreferences("DiapasonPrefs", Context.MODE_PRIVATE)
        currentNote = sharedPrefs.getString("note", "A") ?: "A"
        currentOctave = sharedPrefs.getInt("octave", 4)
        referenceFrequency = sharedPrefs.getFloat("referenceFrequency", 440f).toDouble()
    }

    private fun saveSettings() {
        getSharedPreferences("DiapasonPrefs", Context.MODE_PRIVATE).edit()
            .putString("note", currentNote)
            .putInt("octave", currentOctave)
            .putFloat("referenceFrequency", referenceFrequency.toFloat())
            .apply()
    }

    private fun startTunerAutomatically() {
        if (hasAudioPermission()) {
            startPitchDetection()
        } else {
            requestAudioPermission()
        }
    }

    private fun playDiapasonNote(note: String, octave: Int) {
        stopDiapason()

        val frequency = calculateNoteFrequency(note, octave)
        val sampleRate = 44100
        val duration = 3 // secondi
        val numSamples = duration * sampleRate
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i * frequency / sampleRate
            samples[i] = (sin(angle) * Short.MAX_VALUE).toInt().toShort()
        }

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack?.apply {
            write(samples, 0, samples.size)
            setVolume(AudioTrack.getMaxVolume())
            play()
            isPlaying = true
        }
    }

    private fun stopDiapason() {
        audioTrack?.let {
            if (isPlaying) {
                it.stop()
                isPlaying = false
            }
            it.release()
            audioTrack = null
        }
    }

    private fun calculateNoteFrequency(note: String, octave: Int): Double {
        val notes = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val semitones = notes.indexOf(note) - notes.indexOf("A")
        return referenceFrequency * 2.0.pow((semitones + (octave - 4) * 12) / 12.0)
    }

    private fun startPitchDetection() {
        try {
            val dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0)

            val pitchHandler = PitchDetectionHandler { result, _ ->
                result.pitch.takeIf { it > 0 }?.let { pitch ->
                    updatePitchUI(pitch)
                }
            }

            dispatcher.addAudioProcessor(PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.YIN,
                22050f,
                1024,
                pitchHandler
            ))

            dispatcher.addAudioProcessor(waveView)
            dispatcherThread = Thread(dispatcher, "AudioDispatcher")
            dispatcherThread?.start()
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                tvNote.text = "Errore nel microfono"
            }
        }
    }

    private fun updatePitchUI(pitch: Float) {
        val (noteName, cents) = calculateNoteAndCents(pitch)
        runOnUiThread {
            tvNote.text = noteName
            tvCents.text = if (cents % 1 == 0f) cents.toInt().toString() else "%.1f".format(cents)
            needleView.setCents(cents)
        }
    }

    private fun calculateNoteAndCents(freq: Float): Pair<String, Float> {
        val noteNames = listOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")
        val n = 12 * (ln(freq / referenceFrequency) / ln(2.0))
        val nInt = n.roundToInt()
        val freqNote = referenceFrequency * 2.0.pow(nInt / 12.0)
        val cents = (1200 * ln(freq / freqNote) / ln(2.0)).toFloat()
        val index = ((nInt + 9) % 12 + 12) % 12
        return noteNames[index] to cents.coerceIn(-50f, 50f)
    }


    private fun increaseReferenceFrequency() {
        referenceFrequency *= 2.0.pow(1.0 / 12.0)
        saveSettings()
    }

    private fun decreaseReferenceFrequency() {
        referenceFrequency /= 2.0.pow(1.0 / 12.0)
        saveSettings()
    }

    private fun updateReferenceFreqUI() {
        runOnUiThread {
            tvReferenceFreq.text = "%.2f Hz".format(referenceFrequency)
        }
    }

    private fun hasAudioPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startPitchDetection()
        } else {
            tvNote.text = "Microfono disattivato"
        }
    }

    override fun onSettingsSaved(note: String, octave: Int) {
        currentNote = note
        currentOctave = octave
        saveSettings()
    }

    private fun showSettingsDialog() {
        SettingsDialog().show(supportFragmentManager, "settings_dialog")
    }


    override fun onDestroy() {
        super.onDestroy()
        stopDiapason()
    }
}