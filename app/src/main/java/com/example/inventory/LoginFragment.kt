package com.example.inventory

import android.R
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.inventory.data.ItemRoomDatabase
import com.example.inventory.data.Users
import com.example.inventory.databinding.FragmentLoginBinding
import com.example.inventory.viewmodel.UsersViewModel
import com.example.inventory.viewmodel.UsersViewModelFactory
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val usersViewModel : UsersViewModel by activityViewModels{
        UsersViewModelFactory(
            (activity?.application as InventoryApplication).database.
            usersDao())
    }
    lateinit var user: Users

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        lifecycleScope.launch {
            val userList: List<Users> =
                ItemRoomDatabase.getDatabase(requireContext()).usersDao().getUsers()
            if (userList.isNotEmpty()) {
                loadSpinnerData()
            } else {
                binding.loginBtn.isEnabled = false
            }
        }
        return binding.root
    }
    private fun loadSpinnerData() {
        lifecycleScope.launch {
            val spinner: Spinner = binding.userSpinner
            // database handler
            val db = ItemRoomDatabase.getDatabase(requireContext())

            // Spinner Drop down elements
            val labels: List<String> = db.usersDao().getAllUserNames()

            // Creating adapter for spinner
            val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLoadUsers.setOnClickListener { loadSpinnerData() }
        binding.loginBtn.setOnClickListener {loginAction() }
    }

    private fun loginAction(){
        val currentUser = binding.userSpinner.selectedItem.toString()
        usersViewModel.retrieveMatchingUser(currentUser)
            .observe(this.viewLifecycleOwner) { userid ->
                try {
                    user = userid
                    val sharedPreferences = this.requireActivity()
                        .getSharedPreferences("Users", Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString("user", currentUser)
                    editor.putString("id", user.userId.toString())
                    editor.apply()
                } catch (e: Exception) {
                    println("Nem talált termék")
                }
                if (binding.editPassword.text.toString() == user.userPass) {
                    val action = LoginFragmentDirections.actionLoginFragmentToItemListFragment(
                        currentUser
                    )
                    this.findNavController().navigate(action)
                } else {
                    Toast.makeText(activity, "Hibás jelszó!", Toast.LENGTH_SHORT).show()
                }
            }
    }
}