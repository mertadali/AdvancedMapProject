package com.mertadali.advancedtezproject.view.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.mertadali.advancedtezproject.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        sharedPreferences = requireActivity().getSharedPreferences("com.mertadali.advancedtezproject.view.view",Context.MODE_PRIVATE)

        val rememberMe = sharedPreferences.getBoolean("remember_me",false)
        if (rememberMe){
            val action = LoginFragmentDirections.actionLoginFragmentToPostFragment()
            findNavController().navigate(action)

        }else{

            val currentUser = auth.currentUser
            if (currentUser != null){
                readlnOrNull()
                val action = LoginFragmentDirections.actionLoginFragmentToPostFragment()
                findNavController().navigate(action)
            }
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signupButton.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToSignup()
            findNavController().navigate(action)
        }

        binding.signButton.setOnClickListener {
            val userMail = binding.userMail.text.toString()
            val userPassword = binding.userPassword.text.toString()
            val rememberMe = binding.rememberMeCheckbox.isChecked

            if (userMail != "" && userPassword != ""){
                auth.signInWithEmailAndPassword(userMail,userPassword).addOnCompleteListener {result ->
                    if (result.isSuccessful){

                        val currentUser = auth.currentUser
                        currentUser!!.email.toString()
                        Toast.makeText(requireContext(),"Welcome : ${currentUser.email}",Toast.LENGTH_LONG).show()

                        sharedPreferences.edit().putBoolean("remember_me",rememberMe).apply()

                        val action = LoginFragmentDirections.actionLoginFragmentToPostFragment()
                        findNavController().navigate(action)
                    }


                }.addOnFailureListener {
                    Toast.makeText(requireContext(),"User undefined",Toast.LENGTH_LONG).show()
                }
            }

        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}