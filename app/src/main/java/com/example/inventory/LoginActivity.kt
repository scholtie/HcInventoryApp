package com.example.inventory

import android.R
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.example.inventory.data.ItemRoomDatabase
import com.example.inventory.data.Users
import com.example.inventory.databinding.LoginActivityBinding
import com.example.inventory.service.DWUtilities
import com.example.inventory.viewmodel.UsersViewModel
import com.example.inventory.viewmodel.UsersViewModelFactory
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginActivityBinding
    private lateinit var navController: NavController

    private val usersViewModel : UsersViewModel by viewModels(){
        UsersViewModelFactory(
            (this.application as InventoryApplication).database.
            usersDao())
    }
    lateinit var user: Users

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DWUtilities.CreateDWProfile(this)
        binding = LoginActivityBinding.inflate(layoutInflater)
        val view = binding.root
        binding.btnLoadUsers.setOnClickListener { loadSpinnerData() }
        binding.loginBtn.setOnClickListener {loginAction() }
        setContentView(view)
        lifecycleScope.launch {
            val userList: List<Users> =
                ItemRoomDatabase.getDatabase(this@LoginActivity).usersDao().getUsers()
            if (userList.isNotEmpty()) {
                loadSpinnerData()
            } else {
                binding.loginBtn.isEnabled = false
            }
        }
    }

    private fun loadSpinnerData() {
        lifecycleScope.launch {
            val spinner: Spinner = binding.userSpinner
            // database handler
            val db = ItemRoomDatabase.getDatabase(this@LoginActivity)

            // Spinner Drop down elements
            val labels: List<String> = db.usersDao().getAllUserNames()

            // Creating adapter for spinner
            val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                this@LoginActivity,
                R.layout.simple_spinner_item, labels
            )

            // Drop down layout style - list view with radio button
            dataAdapter
                .setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

            // attaching data adapter to spinner
            spinner.adapter = dataAdapter
        }
        //binding.loginBtn.isEnabled = true
    }

    private fun loginAction(){
        val currentUser = binding.userSpinner.selectedItem.toString()
        usersViewModel.retrieveMatchingUser(currentUser)
            .observe(this) { userid ->
                try {
                    user = userid
                    val sharedPreferences = this
                        .getSharedPreferences("Users", Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString("user", currentUser)
                    editor.putString("id", user.userId.toString())
                    editor.apply()
                } catch (e: Exception) {
                    println("Nem talált termék")
                }
                if (binding.editPassword.text.toString() == user.userPass) {
                    val switchActivityIntent = Intent(this, MainActivity::class.java)
                    startActivity(switchActivityIntent)
                } else {
                    Toast.makeText(this, "Hibás jelszó!", Toast.LENGTH_SHORT).show()
                }
            }
    }
}