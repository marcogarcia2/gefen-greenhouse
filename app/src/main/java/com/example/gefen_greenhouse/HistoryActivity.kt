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
import androidx.core.content.ContextCompat
import com.example.gefen_greenhouse.databinding.ActivityHistoryBinding
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        // Criando o binding
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#BCEDB7")
        window.navigationBarColor = ContextCompat.getColor(this, R.color.dark_green)

        // Buscar o histórico atualizado
        IrrigationSystem.fetchHistory { historyResults ->
            runOnUiThread {
                displayHistory(historyResults)
            }
        }

        binding.homeButton.setOnClickListener {
            Log.d("ActivityMain", "Mudando para a tela de home")
            val intent = Intent(this, MainActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        binding.controlButton.setOnClickListener {
            Log.d("ActivityMain", "Mudando para a tela de controle")
            val intent = Intent(this, ControlActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun displayHistory(historyResults: Map<String, Map<String, Map<String, Any>>>) {
        val container = binding.historyContainer
        container.removeAllViews()

        IrrigationSystem.getStandardHours {
            val sortedHistoryResults = historyResults.toSortedMap(compareByDescending { it })

            // Verifica se existem dados a serem exibidos
            val flag = isNotEmpty(sortedHistoryResults)
            if (!flag) {
                Log.d("VAZIO", "Sem dados do tipo status.")
                val noDataTextView = TextView(this).apply {
                    text = resources.getString(R.string.no_data)
                    textSize = 20f
                    setTextColor(Color.parseColor("#383838"))
                    typeface = resources.getFont(R.font.poppins)
                    gravity = Gravity.CENTER
                }
                container.addView(noDataTextView)
                // Encerra esta função
                return@getStandardHours
            }

            for ((date, results) in sortedHistoryResults) {

                val completeResults = mutableMapOf<String, Map<String, Any>>()

                // Adiciona os horários com dados
                completeResults.putAll(results)

                // Ignora dias em que todos os status são 'N' ou 'I'
                if (completeResults.values.all { timeData ->
                        val status = timeData["status"] as? Char ?: 'I' // Assume 'I' se o status não existir
                        status == 'N' || status == 'I'
                    }) {
                    continue
                }

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

                // Converte a data para o padrão brasileiro
                val formattedDate = try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dateObject = inputFormat.parse(date)
                    outputFormat.format(dateObject)
                } catch (e: Exception) {
                    date
                }

                // Data de cada bloco
                val dateTextView = TextView(this).apply {
                    text = formattedDate // + VASOS DE CADA DIA
                    textSize = 24f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.parseColor("#194215"))
                }
                card.addView(dateTextView)

                // Informação de quantos vasos estavam no experimento em determinado dia
                val nVases = completeResults["vasos"]?.get("vasos") as? Int
                val vasesTextView = TextView(this).apply {
                    text = "$nVases vasos 🪴"
                    textSize = 18f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.parseColor("#194215"))
                }
                card.addView(vasesTextView)

                val sortedCompleteResults = completeResults.toSortedMap()

                for ((time, data) in sortedCompleteResults) {

                    // Pega somente os horários da base, ignorando alguns dados
                    if (time in listOf("total", "vasos")) continue

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
                    val volumeTextView = if (status == 'S' && nVases != null) {
                        TextView(this).apply {
                            text = "${"%.1f".format(volume/nVases)} mL"
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

                // Obtém o volume total do dia
                val totalVolume = (results["total"]?.get("volume") as? Double) ?: 0.0

                if (totalVolume > 0.0){

                    // Criar uma View para o rodapé verde
                    val totalFooter = LinearLayout(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        background = ContextCompat.getDrawable(this@HistoryActivity, R.drawable.rounded_green_background) // Aplica o drawable com bordas arredondadas
                        setPadding(24, 16, 24, 16)
                    }

                    // Texto do total de volume
                    if(nVases != null) {
                        val totalTextView = TextView(this).apply {
                            text = "Total: ${"%.1f".format(totalVolume/nVases)} mL por vaso"
                            textSize = 18f
                            setTypeface(null, Typeface.BOLD)
                            setTextColor(Color.WHITE)
                        }
                        // Adiciona o texto ao rodapé verde
                        totalFooter.addView(totalTextView)
                    }

                    // Adiciona o rodapé verde DENTRO do card branco (último item)
                    card.addView(totalFooter)
                }

                // Adiciona o card ao container principal
                container.addView(card)
            }
        }
    }

    private fun isNotEmpty(historyResults: Map<String, Map<String, Map<String, Any>>>): Boolean {
        return historyResults.any { (_, outerMap) ->
            outerMap.any { (_, innerMap) ->
                innerMap.values.any { it is Char && (it == 'S' || it == 'F') }
            }
        }
    }
}

