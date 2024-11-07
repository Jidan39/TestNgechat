package com.example.testngechat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(userList) { user -> onUserClicked(user) }
        recyclerView.adapter = userAdapter

        // Ambil daftar pengguna dari Firebase
        getUsers()
    }

    private fun getUsers() {
        // Menggunakan ChildEventListener untuk mendapatkan data secara real-time
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    if (it.id != auth.currentUser?.uid) { // Jangan tampilkan pengguna yang sedang login
                        userList.add(it)
                        userAdapter.notifyItemInserted(userList.size - 1)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Update user jika ada perubahan
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    val position = userList.indexOf(it)
                    if (position != -1) {
                        userList.removeAt(position)
                        userAdapter.notifyItemRemoved(position)
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserListActivity, "Failed to load users.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Menangani klik pada user dalam daftar
    private fun onUserClicked(user: User) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("receiverId", user.id)
        startActivity(intent)
    }
}
