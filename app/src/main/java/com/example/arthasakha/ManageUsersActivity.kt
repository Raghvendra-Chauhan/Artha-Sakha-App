package com.example.arthasakha

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ManageUsersActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var userList: MutableList<String>
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        val lvUsers = findViewById<ListView>(R.id.lvUsers)
        database = FirebaseDatabase.getInstance().reference.child("users")
        userList = mutableListOf()

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, userList)
        lvUsers.adapter = adapter

        fetchUsers()
    }

    private fun fetchUsers() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in snapshot.children) {
                    val email = userSnapshot.child("email").getValue(String::class.java)
                    val role = userSnapshot.child("role").getValue(String::class.java) ?: "user"
                    if (email != null) {
                        userList.add("$email ($role)")
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
