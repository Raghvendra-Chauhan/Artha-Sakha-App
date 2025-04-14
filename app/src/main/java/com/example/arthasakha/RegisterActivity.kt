package com.example.arthasakha

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase authentication and database reference
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("users")

        // UI Component References
        val etName = findViewById<EditText>(R.id.etName)
        val etDOB = findViewById<EditText>(R.id.etDOB)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val ivTogglePassword = findViewById<ImageView>(R.id.ivTogglePassword)
        val radioGroupGender = findViewById<RadioGroup>(R.id.radioGroupGender)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvExistingUser = findViewById<TextView>(R.id.tvExistingUser)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        var isPasswordVisible = false

        // Disable manual typing in DOB field, use DatePicker instead
        etDOB.inputType = InputType.TYPE_NULL
        etDOB.setOnClickListener { showDatePicker(etDOB) }

        // Toggle password visibility
        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            etPassword.inputType = if (isPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etPassword.setSelection(etPassword.text.length)
            ivTogglePassword.setImageResource(
                if (isPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
            )
        }

        // Prevent copy-paste in password field
        etPassword.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean = false
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean = false
            override fun onDestroyActionMode(mode: ActionMode?) {}
        })

        // Register Button Click
        btnRegister.setOnClickListener {
            btnRegister.isEnabled = false

            // Get user input
            val name = etName.text.toString().trim()
            val dob = etDOB.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val selectedGenderId = radioGroupGender.checkedRadioButtonId
            val gender = if (selectedGenderId != -1) findViewById<RadioButton>(selectedGenderId).text.toString() else ""

            // Validate inputs before proceeding
            if (!validateInputs(name, dob, username, email, password, selectedGenderId)) {
                btnRegister.isEnabled = true
                return@setOnClickListener
            }

            // Show progress
            updateUI(isLoading = true, btnRegister, progressBar)

            // Check username availability
            checkUsernameAvailability(username) { isAvailable ->
                if (isAvailable) {
                    registerUser(name, dob, username, gender, email, password, btnRegister, progressBar)
                } else {
                    etUsername.error = "Username already taken! Choose another."
                    etUsername.requestFocus()
                    updateUI(isLoading = false, btnRegister, progressBar)
                }
            }
        }

        // Redirect to login page if already registered
        tvExistingUser.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Function to check if the username is already taken
    private fun checkUsernameAvailability(username: String, callback: (Boolean) -> Unit) {
        database.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(!snapshot.exists()) // Username is available if it does not exist
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RegisterActivity, "Error checking username: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        })
    }

    private fun updateUI(isLoading: Boolean, btnRegister: Button, progressBar: ProgressBar) {
        if (isLoading) {
            btnRegister.isEnabled = false
            progressBar.visibility = View.VISIBLE
        } else {
            btnRegister.isEnabled = true
            progressBar.visibility = View.GONE
        }
    }


    // Function to handle user registration
    private fun registerUser(name: String, dob: String, username: String, gender: String, email: String, password: String, btnRegister: Button, progressBar: ProgressBar) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                user?.sendEmailVerification()?.addOnSuccessListener {
                    val role = if (email == "admin@example.com") "admin" else "user" // Admin account check
                    saveUserData(user, name, dob, username, gender, email, role, btnRegister, progressBar)
                }?.addOnFailureListener {
                    Toast.makeText(this, "Email Verification Failed", Toast.LENGTH_SHORT).show()
                    updateUI(isLoading = false, btnRegister, progressBar)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Registration Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                updateUI(isLoading = false, btnRegister, progressBar)
            }
    }

    // Date Picker function
    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this, { _, year, month, day ->
            val selectedDate = Calendar.getInstance().apply { set(year, month, day) }
            if (selectedDate.timeInMillis > System.currentTimeMillis()) {
                Toast.makeText(this, "Invalid Date of Birth", Toast.LENGTH_SHORT).show()
                return@DatePickerDialog
            }
            editText.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun validateInputs(
        name: String, dob: String, username: String,
        email: String, password: String, selectedGenderId: Int
    ): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (dob.isEmpty()) {
            Toast.makeText(this, "Date of Birth is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (username.isEmpty()) {
            Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isEmpty()) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty() || password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }


    // Function to save user data in Firebase
    private fun saveUserData(firebaseUser: FirebaseUser, name: String, dob: String, username: String, gender: String, email: String, role: String, btnRegister: Button, progressBar: ProgressBar) {
        val userId = firebaseUser.uid
        val user = hashMapOf(
            "name" to name,
            "dob" to dob,
            "username" to username,
            "gender" to gender,
            "email" to email,
            "role" to role
        )

        database.child(userId).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration Successful. Verify your email before logging in.", Toast.LENGTH_LONG).show()
                auth.signOut()
                updateUI(isLoading = false, btnRegister, progressBar)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                updateUI(isLoading = false, btnRegister, progressBar)
            }
    }
}
