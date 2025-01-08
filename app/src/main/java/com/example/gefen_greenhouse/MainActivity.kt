package com.example.gefen_greenhouse

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.gefen_greenhouse.databinding.ActivityMainBinding
import java.time.LocalDate


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var irrigationSystem: IrrigationSystem

    // Mapeando cores para os textos
    private val successColor = Color.parseColor("#4BAE4F")
    private val failureColor = Color.parseColor("#FF4141")
    private val waitingColor = Color.parseColor("#ABABAB")

    // Função que traduz o número do mês em seu nome
    private fun getMonth(number: Int): String {
        return when (number) {
            1 -> "Janeiro"
            2 -> "Fevereiro"
            3 -> "Março"
            4 -> "Abril"
            5 -> "Maio"
            6 -> "Junho"
            7 -> "Julho"
            8 -> "Agosto"
            9 -> "Setembro"
            10 -> "Outubro"
            11 -> "Novembro"
            12 -> "Dezembro"
            else -> "Número inválido"
        }
    }

    // Traduz uma cor dependendo da string
    private fun getColorForString(stringId: Int): Int {
        return when (stringId) {
            R.string.sucesso -> Color.parseColor("#4BAE4F") // Verde
            R.string.falhou -> Color.parseColor("#FF4141")  // Vermelho
            R.string.aguardando -> Color.parseColor("#ABABAB") // Cinza
            else -> Color.BLACK // Cor padrão
        }
    }

    // Retorna o texto no formato correto com a cor desejada
    private fun getColoredText(stringId: Int): SpannableString {
        val text = getString(stringId)
        val color = getColorForString(stringId) // Obtém a cor automaticamente
        val spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(color),
            0,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        // Criando o binding
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializando a variável do sistema de irrigação
        irrigationSystem = ViewModelProvider(this).get(IrrigationSystem::class.java)

        // Exibindo a data de hoje
        val today = LocalDate.now()
        val day = today.dayOfMonth
        val month = getMonth(today.monthValue)
        binding.data.text = "${month}, ${day}"

        // Atribuindo o texto correto TESTEEEEEEEEEEEEEEEEEEEEEEE
        irrigationSystem.updateStatus('S')
        irrigationSystem.updateStatus('F')

        // Atualizando os textos com cores
        binding.resultado1.text = getColoredText(irrigationSystem.getStringID(0) ?: R.string.aguardando)
        binding.resultado2.text = getColoredText(irrigationSystem.getStringID(1) ?: R.string.aguardando)
        binding.resultado3.text = getColoredText(irrigationSystem.getStringID(2) ?: R.string.aguardando)


    }
}