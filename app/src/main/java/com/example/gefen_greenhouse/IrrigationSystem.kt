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

    var todayResults = mutableMapOf<String, MutableMap<String, Any>>()
    var today: String = getCurrentDate()
    val statusDict = mapOf(
        'N' to R.string.aguardando,
        'S' to R.string.sucesso,
        'F' to R.string.falhou,
        'I' to R.string.indeterminado
    )

    private var workingTimes: MutableList<String> = mutableListOf()
    private var database: DatabaseReference
    private var password: String = ""
    private val schedulePath: String = "000h"

    // Excpetions:
    class TimeAlreadyExistsException(message: String) : Exception(message)
    class DatabaseAccessError(message: String) : Exception(message)
    class DatabaseInsertError(message: String) : Exception(message)
    class TimeDoesNotExistException(message: String) : Exception(message)


    init {
        // Inicializando o Firebase Database
        database = FirebaseDatabase.getInstance().reference.child("Estufa")

        // Pegando a senha do banco de dados
        database.child("000p").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                password = snapshot.getValue(String::class.java) ?: ""
                Log.d("SENHA", "Senha obtida com sucesso.")
            } else {
                Log.d("SENHA", "Chave 000p não encontrada no banco de dados.")
            }
        }.addOnFailureListener { exception ->
            Log.e("SENHA", "Erro ao buscar a senha: ${exception.message}")
        }
    }


    // Funções de Home

    // Organiza os resultados da leitura de hoje, em um dicionário todayResults.
    fun monitorTodayResults(onUpdate: () -> Unit) {
        var pendingUpdates = 2 // Contador para rastrear atualizações pendentes
        todayResults.clear()
        today = getCurrentDate()

        // Monitorar os resultados do nó correspondente à data de hoje
        database.child(today).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val time = child.key  // Ex: "08:00"

                        // Recupera status e volume do horário específico
                        val status = child.child("status").getValue(String::class.java)?.firstOrNull() ?: 'I'
                        val volume = child.child("volume").getValue(Double::class.java) ?: 0.0

                        if (time != null) {
                            todayResults[time] = mutableMapOf(
                                "status" to status,
                                "volume" to volume
                            )
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
        database.child(schedulePath).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    val time = child.getValue(String::class.java)

                    if (time != null && !todayResults.containsKey(time)) {
                        val timeInSeconds = timeToSeconds(time)
                        if (timeInSeconds != null) {
                            if (getCurrentTimeInSeconds() < timeInSeconds + (5 * 60)) {
                                todayResults[time] = mutableMapOf(
                                    "status" to 'N',
                                    "volume" to 0.0
                                )
                            } else {
                                todayResults[time] = mutableMapOf(
                                    "status" to 'I',
                                    "volume" to 0.0
                                )
                            }
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

    // Converte uma string em segundos
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

    // Converte o tempo atual em segundos
    fun getCurrentTimeInSeconds(): Int {
        val calendar = Calendar.getInstance()
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)
        return hours * 3600 + minutes * 60 + seconds
    }

    // Funções de Histórico

    // Função que organiza e mostra o histórico
    fun fetchHistory(onComplete: (Map<String, Map<String, Map<String, Any>>>) -> Unit) {
        val historyResults = mutableMapOf<String, MutableMap<String, Map<String, Any>>>()

        // Obter os horários padrão
        getStandardHours { standardHours ->
            val datesToFetch = getLastNDates(30) // Obter os últimos 30 dias, incluindo hoje
            var pendingDates = datesToFetch.size

            // Para cada nó de data, existem nós de horários
            for (date in datesToFetch) {
                database.child(date).get().addOnSuccessListener { snapshot ->
                    val resultsForDate = mutableMapOf<String, Map<String, Any>>()

                    // Aqui vamos iterar sobre cada horário de uma data
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val time = child.key
                            val status = child.child("status").getValue(String::class.java)?.firstOrNull() ?: 'I'
                            val volume = child.child("volume").getValue(Double::class.java) ?: 0.0

                            if (time != null) {
                                resultsForDate[time] = mapOf(
                                    "status" to status,
                                    "volume" to volume
                                )
                            }
                        }
                    }

                    // Adiciona os horários padrão como 'N' apenas se não estiverem nos dados do banco no dia de hoje
                    if (date == today){
                        for (time in standardHours) {
                            if (!resultsForDate.containsKey(time)) {
                                resultsForDate[time] = mapOf(
                                    "status" to 'N',
                                    "volume" to 0.0
                                )
                            }
                        }
                    }

                    // Adiciona os resultados para a data no histórico
                    historyResults[date] = resultsForDate

                    pendingDates--
                    if (pendingDates == 0) {
                        // Retorna o histórico ordenado por data (mais recentes primeiro)
                        onComplete(historyResults.toSortedMap(compareByDescending { it }))
                    }

                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Erro ao acessar o nó $date: ${exception.message}")
                    pendingDates--
                    if (pendingDates == 0) {
                        // Retorna o histórico, mesmo com falhas
                        onComplete(historyResults.toSortedMap(compareByDescending { it }))
                    }
                }
            }
        }
    }

    // Função que retorna os horários de funcionamento definidos em 000h em uma lista
    fun getStandardHours(onComplete: (List<String>) -> Unit) {
        val standardHours = mutableListOf<String>()
        database.child(schedulePath).get().addOnSuccessListener { snapshot ->
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

    // Funções de Controle

    // Função que valida a senha inserida pelo usuário
    fun validatePassword(input: String): Boolean {
        return input == password
    }

    // Função que adiciona um horário a database
    fun addTimeToDatabase(input: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {

        val standardHours = mutableListOf<String>()
        database.child(schedulePath).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    child.getValue(String::class.java)?.let { time ->
                        standardHours.add(time)
                    }
                }

                // Verifica se o horário já existe
                if (input in standardHours) {
                    onFailure(TimeAlreadyExistsException("Esse horário já está cadastrado no banco de dados."))
                    return@addOnSuccessListener
                }

                // Adiciona, ordena e atualiza no banco
                standardHours.add(input)
                standardHours.sort()

                val newSchedule = mutableMapOf<String, String>()
                standardHours.forEachIndexed { index, time ->
                    newSchedule[index.toString()] = time
                }

                database.child(schedulePath).setValue(newSchedule).addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener { exception ->
                    onFailure(DatabaseInsertError("Erro ao inserir dados no nó 000h: ${exception.message}"))
                }
            } else {
                // Se não há dados existentes, insere diretamente
                val newEntry = mapOf("0" to input)
                database.child(schedulePath).setValue(newEntry).addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener { exception ->
                    onFailure(DatabaseInsertError("Erro ao inserir o horário: ${exception.message}"))
                }
            }
        }.addOnFailureListener { exception ->
            onFailure(DatabaseAccessError("Erro ao acessar o nó 000h: ${exception.message}"))
        }
    }

    // Função que remove um horário da database
    fun removeTimeFromDatabase(input: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val standardHours = mutableListOf<String>()
        database.child(schedulePath).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    child.getValue(String::class.java)?.let { time ->
                        standardHours.add(time)
                    }
                }

                // Verifica se o horário não existe no BD
                if (input !in standardHours) {
                    onFailure(TimeDoesNotExistException("Esse horário não existe no banco de dados."))
                    return@addOnSuccessListener
                }

                // remove, ordena e atualiza no banco
                standardHours.remove(input)
                standardHours.sort()

                val newSchedule = mutableMapOf<String, String>()
                standardHours.forEachIndexed { index, time ->
                    newSchedule[index.toString()] = time
                }

                database.child(schedulePath).setValue(newSchedule).addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener { exception ->
                    onFailure(DatabaseInsertError("Erro ao inserir dados no nó 000h: ${exception.message}"))
                }

            } else {
                onFailure(TimeDoesNotExistException("Esse horário não existe no banco de dados."))
                return@addOnSuccessListener
            }

        }.addOnFailureListener { exception ->
            onFailure(DatabaseAccessError("Erro ao acessar o nó 000h: ${exception.message}"))
        }
    }

    // Função que retorna uma lista de strings com os horários atuais
    fun getWorkingTimes(onComplete: (List<String>) -> Unit) {
        database.child(schedulePath).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                workingTimes.clear() // Limpa a lista antes de atualizar
                for (child in snapshot.children) {
                    child.getValue(String::class.java)?.let { time ->
                        workingTimes.add(time)
                    }
                }
                workingTimes.sort() // Ordena os horários
                onComplete(workingTimes) // Retorna a lista atualizada no callback
            } else {
                onComplete(emptyList()) // Retorna lista vazia se não houver dados
            }
        }.addOnFailureListener { exception ->
            Log.e("IrrigationSystem", "Erro ao acessar o banco de dados: ${exception.message}")
            onComplete(emptyList()) // Retorna lista vazia em caso de erro
        }
    }
}