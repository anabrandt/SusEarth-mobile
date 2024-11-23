package com.example.SusEarth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

data class SearchResult(val title: String, val siteName: String, val link: String, val documentId: String? = null)

class ResultsAdapter(
    private val results: MutableList<SearchResult>,
    private val isFavoriteScreen: Boolean = false
) : RecyclerView.Adapter<ResultsAdapter.ResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val layoutId = if (isFavoriteScreen) R.layout.item_result_favoritos else R.layout.item_result
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ResultViewHolder(view, isFavoriteScreen)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val result = results[position]
        holder.bind(result)
    }

    override fun getItemCount(): Int = results.size

    inner class ResultViewHolder(itemView: View, private val isFavoriteScreen: Boolean) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.title_text)
        private val siteNameText: TextView = itemView.findViewById(R.id.site_name_text)
        private val linkText: TextView = itemView.findViewById(R.id.link_text)

        private val favoriteButton: Button? = itemView.findViewById(R.id.add_to_favorites_button)
        private val removeButton: Button? = itemView.findViewById(R.id.remove_from_favorites_button)
        private val alterarButton: Button? = itemView.findViewById(R.id.alterar_favorites_button)

        fun bind(result: SearchResult) {
            titleText.text = result.title
            siteNameText.text = result.siteName
            linkText.text = result.link

            val openLink = View.OnClickListener {
                val context = itemView.context
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.link))
                context.startActivity(intent)
            }
            titleText.setOnClickListener(openLink)
            linkText.setOnClickListener(openLink)

            if (isFavoriteScreen) {
                removeButton?.setOnClickListener {
                    removeFromFavorites(result, itemView.context, adapterPosition)
                }

                alterarButton?.setOnClickListener {
                    val context = itemView.context
                    if (result.documentId != null) {
                        val intent = Intent(context, EdicaoActivity::class.java)
                        intent.putExtra("documentId", result.documentId)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Erro: ID do documento não encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                favoriteButton?.setOnClickListener {
                    addToFavorites(result, itemView.context)
                }
            }
        }

        private fun addToFavorites(result: SearchResult, context: Context) {
            val db = FirebaseFirestore.getInstance()
            val favorite = hashMapOf(
                "title" to result.title,
                "siteName" to result.siteName,
                "link" to result.link
            )
            db.collection("favorites")
                .add(favorite)
                .addOnSuccessListener { documentReference ->
                    val updatedResult = result.copy(documentId = documentReference.id)
                    val position = results.indexOf(result)
                    if (position != -1) {
                        results[position] = updatedResult
                        notifyItemChanged(position)
                    }
                    Toast.makeText(context, "Adicionado aos favoritos", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Erro ao adicionar aos favoritos", Toast.LENGTH_SHORT).show()
                }
        }

        private fun removeFromFavorites(result: SearchResult, context: Context, position: Int) {
            val db = FirebaseFirestore.getInstance()
            result.documentId?.let { docId ->
                db.collection("favorites").document(docId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Removido dos favoritos", Toast.LENGTH_SHORT).show()
                        results.removeAt(position)
                        notifyItemRemoved(position)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Erro ao remover dos favoritos", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
