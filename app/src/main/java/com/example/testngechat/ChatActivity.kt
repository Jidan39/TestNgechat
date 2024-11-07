package com.example.testngechat

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ChildEventListener

class ChatActivity : AppCompatActivity() {

    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    // ID penerima chat
    private lateinit var receiverId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Chats")

        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        recyclerView = findViewById(R.id.recyclerView)

        chatAdapter = ChatAdapter(chatMessages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        // Ambil receiverId dari Intent yang dikirim dari activity sebelumnya
        receiverId = intent.getStringExtra("receiverId") ?: ""

        sendButton.setOnClickListener {
            sendMessage()
        }

        listenForMessages()
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString()
        if (messageText.isNotEmpty()) {
            val senderId = auth.currentUser?.uid ?: ""
            val receiverId = this.receiverId

            val message = ChatMessage(
                senderId = senderId,
                receiverId = receiverId,
                message = messageText,
                timestamp = System.currentTimeMillis()
            )

            // Simpan pesan di Firebase
            val chatId = database.child(senderId).child(receiverId).push().key ?: return
            database.child(senderId).child(receiverId).child(chatId).setValue(message)
            database.child(receiverId).child(senderId).child(chatId).setValue(message)

            messageInput.text.clear()
        } else {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listenForMessages() {
        val senderId = auth.currentUser?.uid ?: ""
        val receiverId = this.receiverId

        database.child(senderId).child(receiverId).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    chatMessages.add(chatMessage)
                    chatAdapter.notifyItemInserted(chatMessages.size - 1)
                    recyclerView.scrollToPosition(chatMessages.size - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                // Menampilkan pesan kesalahan jika ada masalah dengan koneksi Firebase
                Toast.makeText(this@ChatActivity, "Failed to load messages.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
