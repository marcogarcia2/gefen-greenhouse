package com.example.gefen_greenhouse

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import kotlinx.coroutines.NonCancellable.children
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

// Classe que abstrai o sistema de irrigação
class IrrigationSystem : ViewModel() {

    var todayResults: MutableMap<String, Char> = mutableMapOf()
    var today: String = getCurrentDate()
    private var database: DatabaseReference
    val statusDict = mapOf(
        'N' to R.string.aguardando,
        'S' to R.string.sucesso,
        'F' to R.string.falhou,
        'I' to R.string.indeterminado
    )
    private var password: String = ""
    private val schedulePath: String = "000h"

    // Excpetions:
    class TimeAlreadyExistsException(message: String) : Exception(message)

    init {
        // Inicializando o Firebase Database
        database = FirebaseDatabase.getInstance().reference.child("Estufa")

        // Pegando a senha do banco de dados
        database.child("000p").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                password = snapshot.getValue(String::class.java) ?: ""
                Log.d("SENHA", "Senha obtida: $password")
            } else {
                Log.d("SENHA", "Chave 000p não encontrada no banco de dados.")
            }
        }.addOnFailureListener { exception ->
            Log.e("SENHA", "Erro ao buscar a senha: ${exception.message}")
        }
    }

    // Organiza os resultados da leitura de hoje, em um dicionário todayResults.
    // --> Destinado a Home do app
    fun monitorTodayResults(onUpdate: () -> Unit) {
        var pendingUpdates = 2 // Contador para rastrear as atualizações pendentes
        todayResults.clear()
        today = getCurrentDate()

        // Atualiza os resultados de hoje do nó correspondente
        database.child(today).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val time = child.key
                        val status = child.getValue(String::class.java)?.firstOrNull() ?: 'I'
                        if (time != null) {
                            todayResults[time] = status
                        }
                    }
                    Log.d("Firebase", "Dados do nó $today atualizados: $todayResults")
                } else {
                    Log.d("Firebase", "Nenhum dado encontrado no nó $today.")
                }

                // Reduz o contador e chama onUpdate se for o último
                pendingUpdates--
                if (pendingUpdates == 0) {
                    onUpdate()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Erro ao monitorar dados do nó $today: ${error.message}")
                pendingUpdates--
                if (pendingUpdates == 0) {
                    onUpdate()
                }
            }
        })

        // Adiciona horários manuais do nó "000h"
        database.child("000h").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    // Essas são as horas de funcionamento definidas no BD
                    val time = child.getValue(String::class.java)

                    if (time != null && !todayResults.containsKey(time)) {
//                        Log.d("000h", "${child.getValue(String::class.java)}")
                        // É um horário que não tem nenhum valor associado mas existe no BD
                        val timeInSeconds = timeToSeconds(time)
                        if (timeInSeconds != null) {
                            if (getCurrentTimeInSeconds() < timeInSeconds + (5 * 60)) {
                                todayResults[time] = 'N'
                            } else {
                                todayResults[time] = 'I'
                            }
//                            Log.d("Firebase", "Valor manual adicionado: $time -> ${todayResults[time]}")
                            todayResults = todayResults.toSortedMap().toMutableMap()
                        }
                    }
                }
            } else {
                Log.d("Firebase", "Nenhum dado encontrado no nó 000h.")
            }

            // Reduz o contador e chama onUpdate se for o último
            pendingUpdates--
            if (pendingUpdates == 0) {
                onUpdate()
            }
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Erro ao acessar o nó 000h: ${exception.message}")
            pendingUpdates--
            if (pendingUpdates == 0) {
                onUpdate()
            }
        }
    }

    // Retorna a data de hoje pelo sistema
    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun timeToSeconds(time: String): Int? {
        val parts = time.split(":")
        if (parts.size == 2) {
            val hours = parts[0].toIntOrNull()
            val minutes = parts[1].toIntOrNull()
            if (hours != null && minutes != null) {
                return hours * 3600 + minutes * 60
            }
        }
        return null // Retorna null se o formato não for válido
    }

    fun getCurrentTimeInSeconds(): Int {
        val calendar = Calendar.getInstance()
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)
        return hours * 3600 + minutes * 60 + seconds
    }

    fun fetchHistory(onComplete: (Map<String, Map<String, Char>>) -> Unit) {
        val historyResults = mutableMapOf<String, MutableMap<String, Char>>()

        // Obter os horários padrão
        getStandardHours { standardHours ->
            // Obter os últimos 30 dias, incluindo hoje
            val datesToFetch = getLastNDates(30)
            var pendingDates = datesToFetch.size

            for (date in datesToFetch) {
                database.child(date).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val resultsForDate = mutableMapOf<String, Char>()

                        for (child in snapshot.children) {
                            val time = child.key
                            val status = child.getValue(String::class.java)?.firstOrNull() ?: 'I'
                            if (time != null) {
                                resultsForDate[time] = status
                            }
                        }

                        // Adiciona os resultados do dia no histórico
                        historyResults[date] = resultsForDate

                    } else if (date == getCurrentDate()) {
                        // Dia de hoje está vazio. Adiciona horários padrão com status 'N'
                        val resultsForToday = mutableMapOf<String, Char>()
                        for (time in standardHours) {
                            resultsForToday[time] = 'N'
                        }
                        historyResults[date] = resultsForToday
                    }

                    pendingDates--
                    if (pendingDates == 0) {
                        onComplete(historyResults.toSortedMap(compareByDescending { it }))
                    }

                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Erro ao acessar o nó $date: ${exception.message}")
                    pendingDates--
                    if (pendingDates == 0) {
                        onComplete(historyResults.toSortedMap(compareByDescending { it }))
                    }
                }
            }
        }
    }

    // Retorna uma data `n` dias antes de uma data fornecida
    private fun getLastNDates(n: Int): List<String> {
        val dates = mutableListOf<String>()
        val calendar = Calendar.getInstance() // Começa com a data de hoje

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (i in 0 until n) {
            dates.add(dateFormat.format(calendar.time)) // Adiciona a data no formato correto
            calendar.add(Calendar.DAY_OF_YEAR, -1) // Vai para o dia anterior
        }

        return dates
    }

    // Função que normaliza com todos os horários da database
    fun getStandardHours(onComplete: (List<String>) -> Unit) {
        val standardHours = mutableListOf<String>()
        database.child("000h").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    child.getValue(String::class.java)?.let { time ->
                        standardHours.add(time)
                    }
                }
                onComplete(standardHours.sorted()) // Retorna os horários ordenados
            } else {
                Log.d("Firebase", "Nenhum horário encontrado em /Estufa/000h/")
                onComplete(emptyList())
            }
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Erro ao acessar o nó 000h: ${exception.message}")
            onComplete(emptyList())
        }
    }


    // Funções de Controle

    // Função que valida a senha inserida pelo usuário
    fun validatePassword(input: String): Boolean {
        return input == password
    }

    fun addTimeToDatabase(input: String) {

    }



}