package com.example.gefen_greenhouse

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.gefen_greenhouse.databinding.ActivityMainBinding
import java.time.LocalDate


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
//    private lateinit var irrigationSystem: IrrigationSystem

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        // Criando o binding
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#BCEDB7")
        window.navigationBarColor = ContextCompat.getColor(this, R.color.dark_green)

        // Inicializando a variável do sistema de irrigação
//        irrigationSystem = ViewModelProvider(this)[IrrigationSystem::class.java]

        // Monitorando o sistema em tempo real
        IrrigationSystem.monitorTodayResults{updateUI()}

        // Configurando o botão de recarregar
        binding.refreshButton.setOnClickListener {
            Log.d("MainActivity", "Recarregando dados...")
            IrrigationSystem.monitorTodayResults { updateUI() } // Força a atualização do banco de dados
        }

        binding.historyButton.setOnClickListener {
            Log.d("ActivityMain", "Mudando para a tela de histórico")
            val intent = Intent(this, HistoryActivity::class.java)
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

    private fun updateUI() {

        // Update nos dados sensíveis
        IrrigationSystem.refreshData()

        // Exibindo a data de hoje
        val today = LocalDate.now()
        val day = today.dayOfMonth
        val month = getMonthString(today.monthValue)
        binding.data.text = "${month}, ${day}"

        val container = binding.dynamicContainer // LinearLayout no XML
        container.removeAllViews() // Limpa as views antigas

        for ((time, data) in IrrigationSystem.todayResults.toSortedMap()) {

            // Ignorando alguns dados não relevantes
            if (time in listOf("vasos")) continue

            val status = data["status"] as? Char ?: 'I'  // Obtém o status (ou 'I' se não existir)
            val volume = data["volume"] as? Double ?: 0.0 // Obtém o volume (ou 0.0 se não existir)

            // Cria o card
            val card = LinearLayout(this).apply {

                val heightInDp = 100 // Ajuste aqui o valor que fica bom na maioria dos celulares
                val heightInPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, heightInDp.toFloat(), resources.displayMetrics
                ).toInt()

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    heightInPx
                ).apply {
                    setMargins(0, 16, 0, 16)
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
                textSize = 28f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f // Peso para alinhamento
                ).apply {
                    marginStart = 24 // Adiciona margem para afastar do canto esquerdo
                }
                gravity = Gravity.CENTER_VERTICAL // Centraliza verticalmente
            }

            // Cria um layout vertical para exibir o status e o volume/mensagem de erro
            val statusContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    2f // Peso maior para ocupar mais espaço
                )
                gravity = Gravity.CENTER
            }

            // Cria o TextView para o status
            val statusTextView = TextView(this).apply {
                text = getColoredText(this@MainActivity, IrrigationSystem.statusDict[status] ?: R.string.indeterminado)
                textSize = 24f
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
            }

            statusContainer.addView(statusTextView) // Adiciona apenas o status

            // Se o status for diferente de 'N' (Aguardando), adiciona uma mensagem abaixo
            if (status !in listOf('N', 'I')) {
                val infoTextView = TextView(this).apply {
                    textSize = 14f
                    setTextColor(Color.BLACK)
                    gravity = Gravity.CENTER
                }

                infoTextView.text = when (status) {
                    'F' -> "A bomba não ligou."
                    'S' -> "Volume: ${"%.1f".format(volume/IrrigationSystem.getNumberOfVases())} mL"
                    else -> "" // Para outros status, não exibe nada
                }

                statusContainer.addView(infoTextView) // Adiciona a info abaixo do status
            }

            // Adiciona os elementos ao card
            card.addView(timeTextView)
            card.addView(statusContainer)

            // Adiciona o card ao contêiner
            container.addView(card)
        }
    }



}