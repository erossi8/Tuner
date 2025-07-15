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


class SettingsDialog : DialogFragment() {


    interface SettingsListener {
        fun onSettingsSaved(note: String, octave: Int)
    }

    private lateinit var listener: SettingsListener
    private lateinit var sharedPreferences: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)

        listener = context as? SettingsListener
            ?: throw ClassCastException("$context deve implementare SettingsListener")


        sharedPreferences = context.getSharedPreferences("DiapasonPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_settings, null)


        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        val btnDecrease = view.findViewById<Button>(R.id.btnDecrease)
        val btnIncrease = view.findViewById<Button>(R.id.btnIncrease)
        val tvOctave = view.findViewById<TextView>(R.id.tvOctave)


        val savedNote = sharedPreferences.getString("note", "A") ?: "A"
        val savedOctave = sharedPreferences.getInt("octave", 4)


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


        tvOctave.text = savedOctave.toString()


        btnDecrease.setOnClickListener {
            val octave = tvOctave.text.toString().toInt()
            if (octave > 1) tvOctave.text = (octave - 1).toString()
        }


        btnIncrease.setOnClickListener {
            val octave = tvOctave.text.toString().toInt()
            if (octave < 8) tvOctave.text = (octave + 1).toString()
        }


        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Impostazioni Diapason")
            .setPositiveButton("OK") { _, _ ->

                val selectedId = radioGroup.checkedRadioButtonId
                val selectedRadio = view.findViewById<RadioButton>(selectedId)
                val noteText = selectedRadio?.text?.toString() ?: "La"


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


                val octave = tvOctave.text.toString().toInt()


                sharedPreferences.edit()
                    .putString("note", note)
                    .putInt("octave", octave)
                    .apply()


                listener.onSettingsSaved(note, octave)
            }
            .setNegativeButton("Annulla", null)
            .create()
    }
}
