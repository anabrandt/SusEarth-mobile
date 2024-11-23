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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {

    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var resultsAdapter: ResultsAdapter
    private val resultsList = mutableListOf<SearchResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val searchInput = findViewById<EditText>(R.id.search_input)
        val searchButton = findViewById<Button>(R.id.button_enviar)
        val favoritesIcon = findViewById<ImageView>(R.id.favorites_icon)

        resultsRecyclerView = findViewById(R.id.results_recycler_view)
        resultsRecyclerView.layoutManager = LinearLayoutManager(this)
        resultsAdapter = ResultsAdapter(resultsList)
        resultsRecyclerView.adapter = resultsAdapter

        // Configurar o clique do botão de pesquisa
        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                searchSerpAPI(query)
            } else {
                Toast.makeText(this, "Por favor, insira uma palavra para buscar", Toast.LENGTH_SHORT).show()
            }
        }

        favoritesIcon.setOnClickListener {
            val intent = Intent(this, FavoritosActivity::class.java)
            startActivity(intent)
        }
    }

    private fun searchSerpAPI(query: String) {
        val apiKey = "1a316514948c0ae8d114b67d500a23bd5282e4564b67ca6c0fa0bd1d1f296e64"
        val url = "https://serpapi.com/search.json?q=$query&api_key=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val jsonData = response.body?.string()
                val jsonObject = JSONObject(jsonData ?: "")
                val resultsArray = jsonObject.getJSONArray("organic_results")

                resultsList.clear()
                for (i in 0 until resultsArray.length()) {
                    val resultObj = resultsArray.getJSONObject(i)
                    val title = resultObj.getString("title")
                    val link = resultObj.getString("link")
                    val siteName = resultObj.optString("displayed_link", "Unknown Site")

                    resultsList.add(SearchResult(title, siteName, link))
                }

                runOnUiThread {
                    resultsAdapter.notifyDataSetChanged()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Erro ao buscar dados", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
