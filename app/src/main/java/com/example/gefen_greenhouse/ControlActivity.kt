package com.example.gefen_greenhouse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.gefen_greenhouse.databinding.ActivityControlBinding
import com.example.gefen_greenhouse.databinding.ActivityHistoryBinding

class ControlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityControlBinding
    private lateinit var irrigationSystem: IrrigationSystem

    class FillAllFieldsException(message: String) : Exception(message)
    class InvalidTimeFormatException(message: String) : Exception(message)
    class WrongPasswordException(message: String) : Exception(message)

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
            val timeInput = binding.editTextText.text.toString()
            val passwordInput = binding.editTextTextPassword.text.toString()

            try {
                verifyInputs(timeInput, passwordInput)
                irrigationSystem.addTimeToDatabase(timeInput)
            } catch (e: Exception) {
                Log.d("Controle", "${e.message}")
            }
        }

        // Listener para o botão Remover
        binding.removeButton.setOnClickListener {
            val timeInput = binding.editTextText.text.toString()
            val passwordInput = binding.editTextTextPassword.text.toString()

            try {
                verifyInputs(timeInput, passwordInput)
//                irrigationSystem.removeTimeFromDatabase(timeInput)
            } catch (e: Exception) {
                Log.d("Controle", "${e.message}")
            }
        }
    }

    // Função que realiza todas as verificações de formato para então tentar adicionar
    private fun verifyInputs(timeInput: String, passwordInput: String){

        if (timeInput.isNotEmpty() && passwordInput.isNotEmpty()) {
            Log.d("Controle", "Todos os campos OK")
        }
        else{
            throw FillAllFieldsException("Preencha todos os campos!")
        }

        if (isValidTimeFormat(timeInput)){
            Log.d("Controle", "Formato OK")
        }
        else {
            throw InvalidTimeFormatException("Formato inválido do horário.")
        }

        if (irrigationSystem.validatePassword(passwordInput)){
            Log.d("Controle", "Senha OK")
        }
        else {
            throw WrongPasswordException("Senha inválida.")
        }
    }

    // Verifica a expressão regular do horário
    private fun isValidTimeFormat(timeInput: String): Boolean {
        val timeRegex = Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
        return timeRegex.matches(timeInput)
    }


}