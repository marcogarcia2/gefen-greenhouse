package com.example.gefen_greenhouse

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.gefen_greenhouse.databinding.ActivityMainBinding
import java.time.LocalDate


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var irrigationSystem: IrrigationSystem

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        // Criando o binding
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializando a variável do sistema de irrigação
        irrigationSystem = ViewModelProvider(this).get(IrrigationSystem::class.java)

        // Monitorando o sistema em tempo real
        irrigationSystem.monitorTodayResults{updateUI()}

        // Configurando o botão de recarregar
        binding.refreshButton.setOnClickListener {
            Log.d("MainActivity", "Recarregando dados...")
            irrigationSystem.monitorTodayResults { updateUI() } // Força a atualização do banco de dados
        }

        binding.historyButton.setOnClickListener {
            Log.d("ActivityMain", "Mudando para a tela de histórico")
            val intent = Intent(this, HistoryActivity::class.java)
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

    private fun updateUI() {
        // Exibindo a data de hoje
        val today = LocalDate.now()
        val day = today.dayOfMonth
        val month = getMonthString(today.monthValue)
        binding.data.text = "${month}, ${day}"

        val container = binding.dynamicContainer // LinearLayout no XML
        container.removeAllViews() // Limpa as views antigas

        for ((time, status) in irrigationSystem.todayResults.toSortedMap()) {

            // Cria o card
            val card = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    250 // Altura do card
                ).apply {
                    setMargins(0, 16, 0, 16) // Margens entre os cards
                }
                orientation = LinearLayout.HORIZONTAL
                setBackgroundResource(R.drawable.white_rectangle)
                setPadding(24, 24, 24, 24)
                gravity = Gravity.CENTER_VERTICAL // Centraliza verticalmente o conteúdo
            }

            // Cria o TextView para o horário
            val timeTextView = TextView(this).apply {
                text = time
                setTextColor(Color.parseColor("#535353"))
                alpha = 0.5f
                textSize = 30f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f // Peso para alinhamento
                ).apply {
                    marginStart = 24 // Adiciona margem para afastar do canto esquerdo
                }
                gravity = Gravity.CENTER_VERTICAL // Garante que o texto do horário fique centralizado verticalmente
            }

            // Cria o TextView para o status
            val statusTextView = TextView(this@MainActivity).apply {
                text = getColoredText(this@MainActivity, irrigationSystem.statusDict[status] ?: R.string.indeterminado) // Aplica o texto formatado
                textSize = 30f // Tamanho da fonte
                setTypeface(null, Typeface.BOLD) // Fonte em negrito
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    2f // Peso maior para ocupar mais espaço e centralizar no restante do espaço
                )
                gravity = Gravity.CENTER // Centraliza completamente no espaço restante
            }


            // Adiciona os TextViews ao card
            card.addView(timeTextView)
            card.addView(statusTextView)

            // Adiciona o card ao contêiner
            container.addView(card)
        }
    }
}