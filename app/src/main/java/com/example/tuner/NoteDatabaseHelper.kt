package com.example.tuner

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NoteDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "NoteDatabase"
        private const val DATABASE_VERSION = 2  // Incrementa la versione per l'update
        const val TABLE_NOTES = "note"
        const val TABLE_TONALITA = "tonalita"
        const val COL_ID = "id"
        const val COL_NOTA = "nota"
        const val COL_FREQUENZA = "frequenza"
        const val COL_TONALITA = "tonalita"
        const val COL_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Tabella originale per le note
        db.execSQL("""
            CREATE TABLE $TABLE_NOTES (
                $COL_NOTA TEXT PRIMARY KEY,
                $COL_FREQUENZA REAL NOT NULL
            )
        """)

        // Nuova tabella per le tonalità salvate
        db.execSQL("""
            CREATE TABLE $TABLE_TONALITA (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TONALITA TEXT NOT NULL,
                $COL_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """)

        // Inserisci le note standard (come prima)
        val noteStandard = listOf(
            "Do" to 261.63, "Do#" to 277.18, "Re" to 293.66,
            "Re#" to 311.13, "Mi" to 329.63, "Fa" to 349.23,
            "Fa#" to 369.99, "Sol" to 392.00, "Sol#" to 415.30,
            "La" to 440.00, "La#" to 466.16, "Si" to 493.88
        )
        noteStandard.forEach { (nota, freq) ->
            db.execSQL("INSERT INTO $TABLE_NOTES VALUES ('$nota', $freq)")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("""
                CREATE TABLE $TABLE_TONALITA (
                    $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_TONALITA TEXT NOT NULL,
                    $COL_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """)
        }
    }

    // Aggiungi una tonalità al database
    fun aggiungiTonalita(tonalita: String) {
        writableDatabase.execSQL("""
            INSERT INTO $TABLE_TONALITA ($COL_TONALITA) 
            VALUES ('$tonalita')
        """)
    }

    // Ottieni tutte le tonalità salvate (ordinate dalla più recente)
    fun getTonalitaSalvate(): List<Pair<Int, String>> {
        val tonalita = mutableListOf<Pair<Int, String>>()
        readableDatabase.rawQuery("""
        SELECT $COL_ID, $COL_TONALITA 
        FROM $TABLE_TONALITA 
        ORDER BY $COL_TIMESTAMP DESC
    """, null).use { cursor ->
            while (cursor.moveToNext()) {
                tonalita.add(cursor.getInt(0) to cursor.getString(1))
            }
        }
        return tonalita
    }

    // Elimina una tonalità
    fun eliminaTonalita(id: Int) {
        writableDatabase.delete(
            TABLE_TONALITA,
            "$COL_ID = ?",
            arrayOf(id.toString())
        )
    }
}