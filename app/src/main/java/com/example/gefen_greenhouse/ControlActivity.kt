package com.example.gefen_greenhouse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import com.example.gefen_greenhouse.databinding.ActivityControlBinding

class ControlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityControlBinding
    private lateinit var irrigationSystem: IrrigationSystem

    private val bannedTimes = listOf("00:00")

    class FillAllFieldsException(message: String) : Exception(message)
    class InvalidTimeFormatException(message: String) : Exception(message)
    class WrongPasswordException(message: String) : Exception(message)
    class BannedTimeException(message: String) : Exception(message)
    class NotMultipleOf10Exception(message: String) : Exception(message)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurando o ViewBinding
        binding = ActivityControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializando o sistema
        irrigationSystem = ViewModelProvider(this).get(IrrigationSystem::class.java)

        binding.homeButton.setOnClickListener {
            Log.d("ActivityMain", "Mudando para a tela de home")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        binding.historyButton.setOnClickListener {
            Log.d("ActivityMain", "Mudando para a tela de histórico")
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        // Listener para o botão Adicionar
        binding.addButton.setOnClickListener {
            val timeInput = binding.editTextText.text.toString().trim()
            val passwordInput = binding.editTextTextPassword.text.toString()

            try {
                verifyInputsToAdd(timeInput, passwordInput)

                irrigationSystem.addTimeToDatabase(
                    timeInput,
                    onSuccess = {
                        Log.d("Controle", "Horário adicionado com sucesso.")
                        showToast("Horário adicionado com sucesso!")

                        // Limpa o texto
                        binding.editTextText.text.clear()
                        binding.editTextTextPassword.text.clear()

                        // Mostra os horários atuais atualizados
                        irrigationSystem.getWorkingTimes { workingTimes ->
                            if (workingTimes.isNotEmpty()) {
                                displayCurrentSchedule(workingTimes) // Atualiza o layout com os horários
                            } else {
                                Log.d("ControlActivity", "Nenhum horário encontrado.")
                            }
                        }

                    },
                    onFailure = { exception ->
                        Log.d("Controle", "Erro: ${exception.message}")
                        showToast(exception.message ?: "Erro ao adicionar horário.")
                    }
                )
            }

            catch (e: Exception) {
                Log.d("Controle", "Erro: ${e.message}")
                showToast(e.message ?: "Erro inesperado.")
            }
        }

        // Listener para o botão Remover
        binding.removeButton.setOnClickListener {
            val timeInput = binding.editTextText.text.toString().trim()
            val passwordInput = binding.editTextTextPassword.text.toString()

            try {
                verifyInputsToRemove(timeInput, passwordInput)

                irrigationSystem.removeTimeFromDatabase(
                    timeInput,
                    onSuccess = {
                        Log.d("Controle", "Horário removido com sucesso.")
                        showToast("Horário removido com sucesso!")

                        // Limpa o texto
                        binding.editTextText.text.clear()
                        binding.editTextTextPassword.text.clear()

                        // Mostra os horários atuais atualizados
                        irrigationSystem.getWorkingTimes { workingTimes ->
                            if (workingTimes.isNotEmpty()) {
                                displayCurrentSchedule(workingTimes) // Atualiza o layout com os horários
                            } else {
                                Log.d("ControlActivity", "Nenhum horário encontrado.")
                            }
                        }
                    },
                    onFailure = { exception ->
                        Log.d("Controle", "Erro: ${exception.message}")
                        showToast(exception.message ?: "Erro ao remover horário.")
                    }
                )

            }

            catch (e: Exception) {
                Log.d("Controle", "Erro: ${e.message}")
                showToast(e.message ?: "Erro inesperado.")
            }
        }

        // Mostra os horários atuais
        irrigationSystem.getWorkingTimes { workingTimes ->
            if (workingTimes.isNotEmpty()) {
                displayCurrentSchedule(workingTimes) // Atualiza o layout com os horários
            } else {
                Log.d("ControlActivity", "Nenhum horário encontrado.")
            }
        }
    }

    // Função que realiza todas as verificações de formato para então tentar adicionar
    private fun verifyInputsToAdd(timeInput: String, passwordInput: String){

        // Verifica se os dois campos foram preenchidos
        if (timeInput.isNotEmpty() && passwordInput.isNotEmpty()) {
            Log.d("Controle", "Todos os campos OK")
        }
        else{
            throw FillAllFieldsException("Preencha todos os campos!")
        }

        // Verifica se o horário foi inserido no formato correto, HH:mm com múltiplo de 10 minutos
        if (isValidTimeFormat(timeInput)){
            Log.d("Controle", "Formato OK")
        }
        else {
            throw InvalidTimeFormatException("Formato inválido do horário. Tente colocar no formato \"HH:mm\".")
        }

        // Verifica se é múltiplo de 10 minutos
        if (isMultipleOf10(timeInput)){
            Log.d("Controle", "Múltiplo 10 OK")
        }
        else {
            throw NotMultipleOf10Exception("O horário precisa ser múltiplo de 10 minutos!")
        }

        // Verifica se o horário inserido está permitido pelo sistema
        if (timeInput !in bannedTimes){
            Log.d("Controle", "Horário Permitido OK")
        }
        else {
            throw BannedTimeException("O horário inserido não é permitido: $timeInput")
        }

        // Por fim, verifica se a senha inserida está correta
        if (irrigationSystem.validatePassword(passwordInput)){
            Log.d("Controle", "Senha OK")
        }
        else {
            throw WrongPasswordException("Senha inválida.")
        }
    }

    // Função que realiza todas as verificações de formato para então tentar adicionar
    private fun verifyInputsToRemove(timeInput: String, passwordInput: String){

        // Verifica se os dois campos foram preenchidos
        if (timeInput.isNotEmpty() && passwordInput.isNotEmpty()) {
            Log.d("Controle", "Todos os campos OK")
        }
        else{
            throw FillAllFieldsException("Preencha todos os campos!")
        }

        // Verifica se o horário foi inserido no formato correto, HH:mm com múltiplo de 10 minutos
        if (isValidTimeFormat(timeInput)){
            Log.d("Controle", "Formato OK")
        }
        else {
            throw InvalidTimeFormatException("Formato inválido do horário. Tente colocar no formato \"HH:mm\".")
        }

        // Por fim, verifica se a senha inserida está correta
        if (irrigationSystem.validatePassword(passwordInput)){
            Log.d("Controle", "Senha OK")
        }
        else {
            throw WrongPasswordException("Senha inválida.")
        }
    }

    // Verifica a expressão regular do horário e se é múltiplo de 10 minutos.
    private fun isValidTimeFormat(timeInput: String): Boolean {
        val timeRegex = Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
        return timeRegex.matches(timeInput)
    }

    // Verifica se é múltiplo de 10
    private fun isMultipleOf10(timeInput: String) : Boolean{
        val minutes = timeInput.split(":")[1].toInt()
        return minutes % 10 == 0
    }

    // Função dos Toasts
    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // Função que atualiza os horários atuais e mostra na tela
    private fun displayCurrentSchedule(schedule: List<String>) {
        val linearHorarios = binding.linearLayoutHorarios
        linearHorarios.removeAllViews()

        for (time in schedule) {
            val timeTextView = TextView(this).apply {
                text = time
                textSize = 18f
                setTextColor(Color.BLACK)
                typeface = resources.getFont(R.font.poppins)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 16)
                }
                gravity = Gravity.CENTER
            }

            linearHorarios.addView(timeTextView)
        }
    }

}