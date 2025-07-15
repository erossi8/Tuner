package com.example.tuner

object TonalitaChecker {

    private val patternMaggiore = listOf(2, 2, 1, 2, 2, 2, 1)           // Maggiore
    private val patternMinoreNaturale = listOf(2, 1, 2, 2, 1, 2, 2)      // Minore naturale
    private val patternMinoreArmonica = listOf(2, 1, 2, 2, 1, 3, 1)      // Minore armonica
    private val patternMinoreMelodica = listOf(2, 1, 2, 2, 2, 2, 1)      // Minore melodica ascendente


    private val ordineNote = listOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")

    fun verificaTonalita(notes: List<String>): String? {
        if (notes.distinct().size != 7) return null


        val noteOrdinate = notes.sortedBy { ordineNote.indexOf(it) }


        for (shift in 0 until noteOrdinate.size) {
            val shiftedNotes = noteOrdinate.drop(shift) + noteOrdinate.take(shift)
            val intervalli = calculateIntervals(shiftedNotes)

            when (intervalli) {
                patternMaggiore -> return "Tonalità maggiore di ${shiftedNotes.first()}"
                patternMinoreNaturale -> return "Tonalità minore naturale di ${shiftedNotes.first()}"
                patternMinoreArmonica -> return "Tonalità minore armonica di ${shiftedNotes.first()}"
                patternMinoreMelodica -> return "Tonalità minore melodica di ${shiftedNotes.first()}"
            }
        }

        return "Non è una tonalità valida"
    }

    private fun calculateIntervals(notes: List<String>): List<Int> {
        val intervals = mutableListOf<Int>()


        for (i in 0 until notes.size - 1) {
            val currentIdx = ordineNote.indexOf(notes[i])
            val nextIdx = ordineNote.indexOf(notes[i + 1])
            intervals.add((nextIdx - currentIdx + 12) % 12)
        }


        if (notes.isNotEmpty()) {
            val firstIdx = ordineNote.indexOf(notes.first())
            val lastIdx = ordineNote.indexOf(notes.last())
            intervals.add((firstIdx - lastIdx + 12) % 12)
        }

        return intervals
    }
}