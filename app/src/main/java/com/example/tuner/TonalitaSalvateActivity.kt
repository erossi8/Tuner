package com.example.tuner

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TonalitaSalvateActivity : AppCompatActivity() {
    private lateinit var dbHelper: NoteDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tonalita_salvate)
        dbHelper = NoteDatabaseHelper(this)
        caricaTonalita()
    }

    private fun caricaTonalita() {
        val container = findViewById<LinearLayout>(R.id.containerTonalita)
        container.removeAllViews()

        dbHelper.getTonalitaSalvate().forEach { (id, tonalita) ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_tonalita, container, false)
            view.findViewById<TextView>(R.id.tvTonalita).text = tonalita
            view.findViewById<Button>(R.id.btnElimina).setOnClickListener {
                dbHelper.eliminaTonalita(id)
                container.removeView(view)
                Toast.makeText(this, "Tonalità eliminata", Toast.LENGTH_SHORT).show()
            }
            container.addView(view)
        }
    }
}