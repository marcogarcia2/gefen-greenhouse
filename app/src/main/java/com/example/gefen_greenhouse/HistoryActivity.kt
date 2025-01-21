package com.example.gefen_greenhouse

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.gefen_greenhouse.databinding.ActivityHistoryBinding
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var irrigationSystem: IrrigationSystem

    override fun onCreate(savedInstanceState: Bundle?) {

        // Criando o Binding
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Iniciando a variável do sistema de irrigação
        irrigationSystem = ViewModelProvider(this).get(IrrigationSystem::class.java)

        // Buscar o histórico dos últimos 7 dias
        irrigationSystem.fetchHistory { historyResults ->
            runOnUiThread {
                displayHistory(historyResults)
            }
        }

        binding.homeButton.setOnClickListener {
            Log.d("ActivityMain", "Mudando para a tela de home")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        binding.controlButton.setOnClickListener {
            Log.d("ActivityMain", "Mudando para a tela de controle")
            val intent = Intent(this, ControlActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun displayHistory(historyResults: Map<String, Map<String, Char>>) {
        val container = binding.historyContainer
        container.removeAllViews()

        // Obtenha os horários padrão de /Estufa/000h/
        irrigationSystem.getStandardHours { standardHours ->
            // Ordenar as datas em ordem decrescente
            val sortedHistoryResults = historyResults.toSortedMap(compareByDescending { it })

            for ((date, results) in sortedHistoryResults) { // Iterar do mais recente para o mais antigo
                val card = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(32, 24, 32, 24) // Reduzindo largura e espaçamento entre os cartões
                    }
                    orientation = LinearLayout.VERTICAL
                    setBackgroundResource(R.drawable.white_rectangle)
                    setPadding(24, 24, 24, 24)
                    gravity = Gravity.CENTER_HORIZONTAL // Centraliza o conteúdo horizontalmente
                }

                val formattedDate = try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Formato de entrada
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Formato de saída
                    val dateObject = inputFormat.parse(date) // Converte a string para um objeto Date
                    outputFormat.format(dateObject) // Formata para o formato brasileiro
                } catch (e: Exception) {
                    date // Retorna o valor original em caso de erro
                }

                val dateTextView = TextView(this).apply {
                    text = formattedDate
                    textSize = 24f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.parseColor("#194215"))
                }
                card.addView(dateTextView)

                // Combine os resultados do histórico com os horários padrão
                val completeResults = mutableMapOf<String, Char>()
                standardHours.forEach { hour ->
                    completeResults[hour] = results[hour] ?: 'N' // Adiciona 'N' se o horário estiver ausente
                }

                for ((time, status) in completeResults) {
                    val row = LinearLayout(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 8, 0, 8) // Espaçamento entre as linhas
                        }
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL // Garante centralização vertical das linhas
                    }

                    val timeTextView = TextView(this).apply {
                        text = time
                        textSize = 18f
                        setTextColor(Color.GRAY)
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        ).apply {
                            marginStart = 16 // Afastar do lado esquerdo
                        }
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    val statusIcon = ImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            50, 50 // Tamanho do ícone
                        ).apply {
                            marginEnd = 16 // Espaçamento lateral do ícone
                            gravity = Gravity.CENTER
                        }
                        when (status) {
                            'S' -> setImageResource(R.drawable.accept)
                            'F' -> setImageResource(R.drawable.delete)
                            else -> setImageResource(R.drawable.empty)
                        }
                    }

                    row.addView(timeTextView)
                    row.addView(statusIcon)

                    card.addView(row)
                }

                container.addView(card)
            }
        }
    }

}
