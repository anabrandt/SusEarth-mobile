package com.example.SusEarth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SenhaNovaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_senha_nova)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val currentPasswordEditText = findViewById<EditText>(R.id.currentPasswordEditText)
        val newPasswordEditText = findViewById<EditText>(R.id.newPasswordEditText)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val loginText = findViewById<TextView>(R.id.loginText)

        sendButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val currentPassword = currentPasswordEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()

            if (email.isNotEmpty() && currentPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                auth.signInWithEmailAndPassword(email, currentPassword).addOnCompleteListener { signInTask ->
                    if (signInTask.isSuccessful) {
                        val user = auth.currentUser
                        user?.reauthenticate(credential)?.addOnCompleteListener { reauthTask ->
                            if (reauthTask.isSuccessful) {
                                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener { documents ->
                                            for (document in documents) {
                                                db.collection("users").document(document.id).update("senha", newPassword)
                                                Toast.makeText(this, "Senha atualizada com sucesso!", Toast.LENGTH_LONG).show()
                                                startActivity(Intent(this, MainActivity::class.java))
                                                finish()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "E-mail ou senha atual incorretos.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            }
        }

        loginText.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
