package com.mertadali.advancedtezproject.view.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.mertadali.advancedtezproject.databinding.FragmentLoginBinding
import com.mertadali.advancedtezproject.databinding.FragmentSignupBinding


class Signup : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedPicture : Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       auth = Firebase.auth
        registerLauncher()



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signupButton.setOnClickListener {

            val userName= binding.userName.text.toString()
            val userMail = binding.userEmail.text.toString()
            val userPassword = binding.userPassword.text.toString()
            val userConfirmPassword = binding.userConfirmPassword.text.toString()

            auth.createUserWithEmailAndPassword(userMail,userPassword).addOnCompleteListener {
                if (userPassword == userConfirmPassword){
                    if (it.isSuccessful){
                        Toast.makeText(activity,"User Created", Toast.LENGTH_LONG).show()
                        val action = SignupDirections.actionSignupToLoginFragment()
                        findNavController().navigate(action)
                    }
                }else{
                    Toast.makeText(activity,"False Password",Toast.LENGTH_LONG).show()
                }

            }.addOnFailureListener {
                Toast.makeText(activity,it.localizedMessage, Toast.LENGTH_LONG).show()
            }

        }
        binding.profileImage.setOnClickListener {

            if (Build.VERSION.SDK_INT == 28){
                if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
                        // Permission request
                        Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                        }).show()

                    }else{
                        // permission request
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                    }

                }else{
                    // permission granted - ContextCompat
                    val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                }
            }

        }
    }


    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == AppCompatActivity.RESULT_OK){
                val intentFromResult = it.data
                if (intentFromResult != null){
                    selectedPicture = intentFromResult.data
                    selectedPicture?.let {
                        binding.profileImage.setImageURI(it)
                    }
                }

            }else if (it.resultCode == AppCompatActivity.RESULT_CANCELED){
                Toast.makeText(requireContext(),"Permission needed for galllery",Toast.LENGTH_LONG).show()
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if (it){
                // request granted
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }


    }

}