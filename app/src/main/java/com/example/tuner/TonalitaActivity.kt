package com.example.tuner

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity



class TonalitaActivity : AppCompatActivity() {
    private lateinit var dbHelper: NoteDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tonalita)

        dbHelper = NoteDatabaseHelper(this)

        val note = arrayOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, note)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinnerIds = arrayOf(
            R.id.spinnerNote1, R.id.spinnerNote2, R.id.spinnerNote3,
            R.id.spinnerNote4, R.id.spinnerNote5, R.id.spinnerNote6, R.id.spinnerNote7
        )

        spinnerIds.forEach { id ->
            findViewById<Spinner>(id).adapter = adapter
        }

        findViewById<Button>(R.id.btnVerifica).setOnClickListener {
            val selectedNotes = spinnerIds.map { findViewById<Spinner>(it).selectedItem.toString() }

            if (selectedNotes.distinct().size != 7) {
                Toast.makeText(this, "Inserisci 7 note diverse", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = TonalitaChecker.verificaTonalita(selectedNotes)
            findViewById<TextView>(R.id.tvRisultato).text = result ?: "Non è una tonalità valida"


            result?.let { tonalita ->
                NoteDatabaseHelper(this).aggiungiTonalita(tonalita)
                Toast.makeText(this, "Tonalità salvata!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}