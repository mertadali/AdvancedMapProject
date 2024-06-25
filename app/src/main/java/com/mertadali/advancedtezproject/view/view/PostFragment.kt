package com.mertadali.advancedtezproject.view.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.installations.ktx.installations
import com.google.firebase.ktx.Firebase
import com.mertadali.advancedtezproject.R
import com.mertadali.advancedtezproject.databinding.FragmentPostBinding


class PostFragment : Fragment()  {
    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        auth = Firebase.auth

        sharedPreferences = requireActivity().getSharedPreferences("com.mertadali.advancedtezproject.view.view",Context.MODE_PRIVATE)


    }

    @Deprecated("Deprecated in Java")

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
      inflater.inflate(R.menu.post_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_place){
            val action = PostFragmentDirections.actionPostFragmentToMapsFragment()
            findNavController().navigate(action)



            // intent yapılacak. -> Fragment Mapse
        }else if(item.itemId == R.id.signout){
            // çıkış işlemi -> login page geri dön

            auth.signOut()

            sharedPreferences.edit().remove("remember_me").apply()

            val action = PostFragmentDirections.actionPostFragmentToLoginFragment()
            findNavController().navigate(action)



        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        val view = binding.root
        return view

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




}