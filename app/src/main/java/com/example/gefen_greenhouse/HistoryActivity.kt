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
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        irrigationSystem = ViewModelProvider(this).get(IrrigationSystem::class.java)

        // Buscar o histórico atualizado
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

    private fun displayHistory(historyResults: Map<String, Map<String, Map<String, Any>>>) {
        val container = binding.historyContainer
        container.removeAllViews()

        irrigationSystem.getStandardHours { standardHours ->
            val sortedHistoryResults = historyResults.toSortedMap(compareByDescending { it })

            for ((date, results) in sortedHistoryResults) {
                val completeResults = mutableMapOf<String, Map<String, Any>>()

                // Adiciona os horários com dados
                completeResults.putAll(results)

                // Ignora dias que só possuem status 'N'
                if (completeResults.values.all { it["status"] == 'N' }) continue

                // Criar card do dia
                val card = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(32, 24, 32, 24)
                    }
                    orientation = LinearLayout.VERTICAL
                    setBackgroundResource(R.drawable.white_rectangle)
                    setPadding(24, 24, 24, 24)
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                val formattedDate = try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dateObject = inputFormat.parse(date)
                    outputFormat.format(dateObject)
                } catch (e: Exception) {
                    date
                }

                val dateTextView = TextView(this).apply {
                    text = formattedDate
                    textSize = 24f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.parseColor("#194215"))
                }
                card.addView(dateTextView)

                val sortedCompleteResults = completeResults.toSortedMap()

                for ((time, data) in sortedCompleteResults) {
                    val status = data["status"] as? Char ?: 'I'
                    val volume = data["volume"] as? Double ?: 0.0

                    val row = LinearLayout(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 8, 0, 8)
                        }
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
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
                            marginStart = 16
                        }
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    // Exibir volume somente se o status for 'S'
                    val volumeTextView = if (status == 'S') {
                        TextView(this).apply {
                            text = "${"%.1f".format(volume)} mL"
                            textSize = 16f
                            setTextColor(Color.BLACK)
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            gravity = Gravity.CENTER
                        }
                    } else null

                    val statusIcon = ImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            50, 50
                        ).apply {
                            marginEnd = 16
                            gravity = Gravity.CENTER
                        }
                        when (status) {
                            'S' -> setImageResource(R.drawable.accept)
                            'F' -> setImageResource(R.drawable.delete)
                            else -> setImageResource(R.drawable.empty)
                        }
                    }

                    row.addView(timeTextView)
                    volumeTextView?.let { row.addView(it) } // Adiciona volume somente se for sucesso
                    row.addView(statusIcon)

                    card.addView(row)
                }

                container.addView(card)
            }
        }
    }
}
