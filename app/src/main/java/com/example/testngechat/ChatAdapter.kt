package com.example.testngechat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(private val chatMessages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        holder.bind(chatMessage)
    }

    override fun getItemCount() = chatMessages.size

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)

        fun bind(chatMessage: ChatMessage) {
            messageTextView.text = chatMessage.message

            // Cek apakah pesan ini dari pengirim saat ini atau penerima
            if (chatMessage.senderId == currentUserId) {
                // Pesan yang dikirim oleh pengguna aktif
                messageTextView.setBackgroundResource(android.R.drawable.dialog_holo_light_frame) // Background untuk pesan dikirim
                messageTextView.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.black)) // Warna teks untuk pesan dikirim
            } else {
                // Pesan yang diterima oleh pengguna aktif
                messageTextView.setBackgroundResource(android.R.drawable.btn_default_small) // Background untuk pesan diterima
                messageTextView.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white)) // Warna teks untuk pesan diterima
            }
        }
    }
}
