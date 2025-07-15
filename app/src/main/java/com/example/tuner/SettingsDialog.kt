package com.example.tuner

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

// Dialog per modificare le impostazioni del diapason (nota e ottava)
class SettingsDialog : DialogFragment() {

    // Interfaccia per comunicare al MainActivity le modifiche salvate
    interface SettingsListener {
        fun onSettingsSaved(note: String, octave: Int)
    }

    private lateinit var listener: SettingsListener
    private lateinit var sharedPreferences: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Associa il listener all'activity che implementa SettingsListener
        listener = context as? SettingsListener
            ?: throw ClassCastException("$context deve implementare SettingsListener")

        // Ottiene SharedPreferences per leggere e scrivere impostazioni
        sharedPreferences = context.getSharedPreferences("DiapasonPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflata la vista personalizzata per il dialog
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_settings, null)

        // Trova i componenti della UI
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        val btnDecrease = view.findViewById<Button>(R.id.btnDecrease)
        val btnIncrease = view.findViewById<Button>(R.id.btnIncrease)
        val tvOctave = view.findViewById<TextView>(R.id.tvOctave)

        // Carica le impostazioni salvate (nota e ottava)
        val savedNote = sharedPreferences.getString("note", "A") ?: "A"
        val savedOctave = sharedPreferences.getInt("octave", 4)

        // Imposta la nota selezionata nel gruppo radio corrispondente
        val radioButtonId = when (savedNote) {
            "C" -> R.id.radioC
            "C#" -> R.id.radioCSharp
            "D" -> R.id.radioD
            "D#" -> R.id.radioDSharp
            "E" -> R.id.radioE
            "F" -> R.id.radioF
            "F#" -> R.id.radioFSharp
            "G" -> R.id.radioG
            "G#" -> R.id.radioGSharp
            "A" -> R.id.radioA
            "A#" -> R.id.radioASharp
            "B" -> R.id.radioB
            else -> R.id.radioA
        }
        radioGroup.check(radioButtonId)

        // Imposta il testo dell’ottava
        tvOctave.text = savedOctave.toString()

        // Pulsante per diminuire ottava (minimo 1)
        btnDecrease.setOnClickListener {
            val octave = tvOctave.text.toString().toInt()
            if (octave > 1) tvOctave.text = (octave - 1).toString()
        }

        // Pulsante per aumentare ottava (massimo 8)
        btnIncrease.setOnClickListener {
            val octave = tvOctave.text.toString().toInt()
            if (octave < 8) tvOctave.text = (octave + 1).toString()
        }

        // Costruisce e ritorna il dialog con pulsanti OK e Annulla
        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Impostazioni Diapason")
            .setPositiveButton("OK") { _, _ ->
                // Quando si clicca OK, salva le impostazioni selezionate

                // Ottiene la nota selezionata
                val selectedId = radioGroup.checkedRadioButtonId
                val selectedRadio = view.findViewById<RadioButton>(selectedId)
                val noteText = selectedRadio?.text?.toString() ?: "La"

                // Mappa il testo della nota in notazione anglosassone
                val note = when (noteText) {
                    "Do" -> "C"
                    "Do#" -> "C#"
                    "Re" -> "D"
                    "Re#" -> "D#"
                    "Mi" -> "E"
                    "Fa" -> "F"
                    "Fa#" -> "F#"
                    "Sol" -> "G"
                    "Sol#" -> "G#"
                    "La" -> "A"
                    "La#" -> "A#"
                    "Si" -> "B"
                    else -> "A"
                }

                // Ottiene l’ottava selezionata
                val octave = tvOctave.text.toString().toInt()

                // Salva le preferenze
                sharedPreferences.edit()
                    .putString("note", note)
                    .putInt("octave", octave)
                    .apply()

                // Comunica al listener che le impostazioni sono state salvate
                listener.onSettingsSaved(note, octave)
            }
            .setNegativeButton("Annulla", null)
            .create()
    }
}
