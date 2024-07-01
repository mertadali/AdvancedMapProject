    package com.mertadali.advancedtezproject.view.view

    import android.annotation.SuppressLint
    import android.app.Activity
    import android.content.Context
    import android.content.Intent
    import android.content.IntentSender
    import android.content.SharedPreferences
    import android.os.Bundle
    import android.text.method.PasswordTransformationMethod
    import android.view.LayoutInflater
    import android.view.MotionEvent
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.fragment.app.Fragment
    import androidx.navigation.fragment.findNavController
    import com.google.android.gms.auth.api.identity.BeginSignInRequest
    import com.google.android.gms.auth.api.identity.Identity
    import com.google.android.gms.auth.api.identity.SignInClient
    import com.google.firebase.Firebase
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.FirebaseUser
    import com.google.firebase.auth.GoogleAuthProvider
    import com.google.firebase.auth.auth
    import com.mertadali.advancedtezproject.R
    import com.mertadali.advancedtezproject.databinding.FragmentLoginBinding


    class LoginFragment : Fragment() {

        private var _binding: FragmentLoginBinding? = null
        private val binding get() = _binding!!
        private lateinit var auth: FirebaseAuth
        private lateinit var sharedPreferences: SharedPreferences
        private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
        private lateinit var oneTapClient: SignInClient


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            auth = Firebase.auth

            sharedPreferences = requireActivity().getSharedPreferences("com.mertadali.advancedtezproject.view.view",Context.MODE_PRIVATE)

           checkUserSession()
        }
        private fun checkUserSession(){
            val rememberMe = sharedPreferences.getBoolean("remember_me",false)
            if (rememberMe){
                val action = LoginFragmentDirections.actionLoginFragmentToPostFragment()
                findNavController().navigate(action)

            }else{

                val currentUser = auth.currentUser
                if (currentUser != null){
                    updateUI(currentUser)
                }
            }

        }
        private fun updateUI(user: FirebaseUser?) {
            if (user != null) {
                val action = LoginFragmentDirections.actionLoginFragmentToPostFragment()
                findNavController().navigate(action)
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



        @SuppressLint("ClickableViewAccessibility")
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            binding.userPassword.setOnTouchListener { v, event ->
                val DRAWABLE_RIGHT = 2
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= (binding.userPassword.right - binding.userPassword.compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                        if (binding.userPassword.transformationMethod == null) {
                            binding.userPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                            binding.userPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.password_icon, 0)
                        } else {
                            binding.userPassword.transformationMethod = null
                            binding.userPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.password_icon, 0)
                        }
                        return@setOnTouchListener true
                    }
                }
                false
            }
            oneTapClient = Identity.getSignInClient(requireActivity())
            binding.googleSignInBtn.setOnClickListener {
                val signInRequest = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                       BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(getString(R.string.web_client_id))
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .setAutoSelectEnabled(true)
                    .build()

                oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener { intentSenderResponse ->
                        try {
                            startIntentSenderForResult(
                              intentSenderResponse.pendingIntent.intentSender,
                                REQ_ONE_TAP,
                                null,
                                0,
                                0,
                                0,
                                null
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            Toast.makeText(
                                requireContext(),
                                "Failed to start sign-in intent.",
                                Toast.LENGTH_SHORT
                            ).show()
                            e.printStackTrace()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Failed to get sign-in intent: ${e.message}",
                            Toast.LENGTH_SHORT

                        ).show()
                        println(e.message)
                    }
            }




            binding.signupButton.setOnClickListener {
                val action = LoginFragmentDirections.actionLoginFragmentToSignup()
                findNavController().navigate(action)
            }

            binding.signButton.setOnClickListener {
                println(binding.signButton)
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




        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == REQ_ONE_TAP) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        // Handle successful sign-in
                        try {
                            if (data != null) {
                                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                                val idToken = credential.googleIdToken
                                if (idToken != null) {
                                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                                    auth.signInWithCredential(firebaseCredential)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                // Sign in success, update UI with the signed-in user's information
                                                val user = auth.currentUser
                                                Toast.makeText(requireContext(), "Welcome: ${user?.email}", Toast.LENGTH_LONG).show()
                                                updateUI(user)
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Toast.makeText(requireContext(), "Sign in failed.", Toast.LENGTH_LONG).show()
                                                updateUI(null)
                                            }
                                        }
                                } else {
                                    Toast.makeText(requireContext(), "No ID token received.", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Sign-in data is null.", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        // Handle cancelled sign-in
                        Toast.makeText(requireContext(), "Sign-in cancelled.", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        // Handle other results if needed
                    }
                }
            }
        }



        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }






