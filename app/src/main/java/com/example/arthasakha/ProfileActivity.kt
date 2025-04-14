package com.example.arthasakha

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storageRef: StorageReference

    private lateinit var imgProfile: ImageView
    private lateinit var btnChangeProfilePic: ImageButton
    private lateinit var edtName: EditText
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar

    private var imageUri: Uri? = null  // Stores selected image URI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // ✅ Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("users")
        storageRef = FirebaseStorage.getInstance().reference

        // ✅ Initialize UI Components
        imgProfile = findViewById(R.id.imgProfile)
        btnChangeProfilePic = findViewById(R.id.btnChangeProfilePic)
        edtName = findViewById(R.id.edtName)
        btnSave = findViewById(R.id.btnSave)
        progressBar = findViewById(R.id.progressBar)

        // ✅ Load user details from Firebase
        loadUserProfile()

        // ✅ Open image picker when clicking Change Profile Picture button
        btnChangeProfilePic.setOnClickListener {
            openImagePicker()
        }

        // ✅ Save changes when clicking the Save button
        btnSave.setOnClickListener {
            saveUserProfile()
        }
    }

    /**
     * ✅ Load user profile from Firebase
     */
    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            progressBar.visibility = View.VISIBLE
            database.child(user.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressBar.visibility = View.GONE
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").getValue(String::class.java) ?: ""
                        val profilePicUrl = snapshot.child("profilePic").getValue(String::class.java)

                        edtName.setText(name)

                        if (!profilePicUrl.isNullOrEmpty()) {
                            Glide.with(this@ProfileActivity)
                                .load(profilePicUrl)
                                .placeholder(R.drawable.ic_profile)
                                .into(imgProfile)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    /**
     * ✅ Open Image Picker to select profile picture
     */
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imgProfile.setImageURI(imageUri)
        }
    }

    /**
     * ✅ Save user profile changes to Firebase
     */
    private fun saveUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            val name = edtName.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return
            }

            progressBar.visibility = View.VISIBLE

            // ✅ Update user name in Firebase Database
            database.child(user.uid).child("name").setValue(name).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // ✅ If new profile picture is selected, upload it
                    if (imageUri != null) {
                        uploadProfilePicture(user.uid)
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * ✅ Upload profile picture to Firebase Storage
     */
    private fun uploadProfilePicture(userId: String) {
        val profileImageRef = storageRef.child("profile_pictures/$userId.jpg")

        profileImageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                    database.child(userId).child("profilePic").setValue(uri.toString()).addOnCompleteListener { task ->
                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
