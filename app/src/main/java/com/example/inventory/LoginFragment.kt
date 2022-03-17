package com.example.inventory

import android.R
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.inventory.databinding.FragmentLoginBinding
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinner: Spinner = binding.userSpinner
        val file = File(this.requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ugyintezo.txt")
        val text = StringBuilder()
        var br: BufferedReader? = null
        val characters =
            arrayOfNulls<String>(1024) //just an example - you have to initialize it to be big enough to hold all the lines!
        try {
            br = BufferedReader(FileReader(file))
            var sCurrentLine: String?
            var i = 0
            while (br.readLine().also { sCurrentLine = it } != null) {
                val arr = sCurrentLine!!.split(";").toTypedArray()
                //for the first line it'll print
                //text.append("arr[id] = " + arr[0]) // 20000008
                println(arr[1]) // username
                /*text.append("arr[empty] = " + arr[2]) //
                text.append("arr[false] = " + arr[3]) // false
                text.append("arr[true] = " + arr[4]) // true*/

                val spinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                    requireContext(), R.layout.simple_spinner_dropdown_item,
                    arr
                ) //selected item will look like a spinner set from XML

                spinnerArrayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                spinner.adapter = spinnerArrayAdapter
// Create an ArrayAdapter using the string array and a default spinner layout
                /*ArrayAdapter.createFromResource(
                    requireActivity(),
                    R.array.roles_array,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    // Apply the adapter to the spinner
                    spinner.adapter = adapter
                }
*/
                //Now if you want to enter them into separate arrays
                characters[i] = arr[0]
                // and you can do the same with
                // names[1] = arr[1]
                //etc
                i++
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                br?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        /*val spinner: Spinner = binding.userSpinner
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.roles_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }*/
        binding.loginBtn.isEnabled = true



        binding.loginBtn.setOnClickListener {
            val currentUser = binding.userSpinner.selectedItem.toString()
            if(binding.editPassword.text.toString() == "")
            {
                val action = LoginFragmentDirections.actionLoginFragmentToItemListFragment(currentUser)
                this.findNavController().navigate(action)
                val sharedPreferences = this.requireActivity().getSharedPreferences("Users", Context.MODE_PRIVATE)
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("user", currentUser)
                editor.apply()
            }
            else{Toast.makeText(activity, "Hibás jelszó!", Toast.LENGTH_SHORT).show() }
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