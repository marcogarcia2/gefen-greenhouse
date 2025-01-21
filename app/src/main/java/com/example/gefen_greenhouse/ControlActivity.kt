package com.example.gefen_greenhouse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gefen_greenhouse.databinding.ActivityControlBinding
import com.example.gefen_greenhouse.databinding.ActivityHistoryBinding

class ControlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityControlBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurando o ViewBinding
        binding = ActivityControlBinding.inflate(layoutInflater)
        setContentView(binding.root)



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
    }
}