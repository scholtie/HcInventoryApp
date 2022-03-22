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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val usersViewModel : UsersViewModel by activityViewModels{
        UsersViewModelFactory(
            (activity?.application as InventoryApplication).database.
            usersDao())
    }
    lateinit var user: Users

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root

    }
    private fun loadSpinnerData() {
        lifecycleScope.launch {
            val spinner: Spinner = binding.userSpinner
            // database handler
            val db = ItemRoomDatabase.getDatabase(requireContext())

            // Spinner Drop down elements
            val lables: List<String> = db.usersDao().getAllUserNames()

            // Creating adapter for spinner
            val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                R.layout.simple_spinner_item, lables
            )

            // Drop down layout style - list view with radio button
            dataAdapter
                .setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

            // attaching data adapter to spinner
            spinner.setAdapter(dataAdapter)
        }
        binding.loginBtn.isEnabled = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val spinner: Spinner = binding.userSpinner
        binding.btnLoadUsers.setOnClickListener { loadSpinnerData() }

                binding.loginBtn.setOnClickListener {
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
                    if (binding.editPassword.text.toString() == "") {
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
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}