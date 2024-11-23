package com.example.SusEarth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class EdicaoActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var documentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edicao)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val novoNomeEditText = findViewById<EditText>(R.id.novoNomeEditText)
        val botaoAlterar = findViewById<Button>(R.id.botaoAlterar)
        val homeIcon = findViewById<ImageView>(R.id.home_icon)
        val favoritesIcon = findViewById<ImageView>(R.id.favorites_icon)

        documentId = intent.getStringExtra("documentId")

        if (documentId == null) {
            Toast.makeText(this, "Erro: ID do documento não encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }

        botaoAlterar.setOnClickListener {
            val novoNome = novoNomeEditText.text.toString().trim()
            if (novoNome.isNotEmpty()) {
                atualizarNomeNoFirebase(novoNome)
            } else {
                Toast.makeText(this, "Por favor, insira um novo nome", Toast.LENGTH_SHORT).show()
            }
        }

        homeIcon.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        favoritesIcon.setOnClickListener {
            val intent = Intent(this, FavoritosActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun atualizarNomeNoFirebase(novoNome: String) {
        documentId?.let { id ->
            db.collection("favorites").document(id)
                .update("title", novoNome)
                .addOnSuccessListener {
                    Toast.makeText(this, "Nome atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, FavoritosActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao atualizar nome: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
