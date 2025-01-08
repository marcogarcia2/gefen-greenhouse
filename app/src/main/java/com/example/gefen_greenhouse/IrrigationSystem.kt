package com.example.gefen_greenhouse

import android.util.Log
import androidx.lifecycle.ViewModel

// Classe que abstrai o sistema de irrigação
class IrrigationSystem : ViewModel() {

    private var pointer : Int = 0
    var irrigationStatus = charArrayOf('N', 'N', 'N') // Valores: "n", "f", "s"


    fun updateStatus(newStatus: Char) {
        when (newStatus) {
            'N', 'F', 'S' -> {
                irrigationStatus[pointer] = newStatus
                pointer++
            }
            else -> {
                Log.e("UpdateStatus", "Status inválido: $newStatus")
            }
        }
    }

    fun getStringID(number: Int): Int? {
        // Mapeia os status para os recursos de string
        val statusDict = mapOf(
            'N' to R.string.aguardando,
            'S' to R.string.sucesso,
            'F' to R.string.falhou
        )

        // Certifique-se de que irrigationStatus[number] é válido
        val status = irrigationStatus.getOrNull(number)

        // Retorna o ID do recurso de string correspondente ou null
        return status?.let { statusDict[it] }
    }


    fun reset(){
        pointer = 0
        irrigationStatus = charArrayOf('n', 'n', 'n')
    }
}