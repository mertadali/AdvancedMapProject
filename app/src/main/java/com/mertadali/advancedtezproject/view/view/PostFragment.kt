package com.mertadali.advancedtezproject.view.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mertadali.advancedtezproject.R
import com.mertadali.advancedtezproject.databinding.FragmentPostBinding
import com.mertadali.advancedtezproject.view.adapter.PlaceAdapter
import com.mertadali.advancedtezproject.view.model.Place
import com.mertadali.advancedtezproject.view.model.Post
import com.mertadali.advancedtezproject.view.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class PostFragment : Fragment() {
    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private val compositeDisposable = CompositeDisposable()
    private lateinit var feedAdapter: PlaceAdapter
    private var postArrayList: ArrayList<Post> = ArrayList()
    private lateinit var firestoreDatabase: FirebaseFirestore
    private lateinit var placeList: ArrayList<Place>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        auth = Firebase.auth
        firestoreDatabase = Firebase.firestore
        placeList = ArrayList()
        sharedPreferences = requireActivity().getSharedPreferences("com.mertadali.advancedtezproject.view.view", Context.MODE_PRIVATE)
        val db = Room.databaseBuilder(requireContext(), PlaceDatabase::class.java, "Places").build()
        val dao = db.placeDao()
        compositeDisposable.add(
            dao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse, { error ->
                    Toast.makeText(requireActivity(), "Error retrieving data: ${error.message}", Toast.LENGTH_LONG).show()
                })
        )
        feedAdapter = PlaceAdapter(ArrayList())
        getData()
    }

    private fun handleResponse(placeList: List<Place>) {
        this.placeList = placeList as ArrayList<Place> // placeList'i atama
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        feedAdapter = PlaceAdapter(placeList) // Adapter'i güncelle
        binding.recyclerView.adapter = feedAdapter
        feedAdapter.setPostList(postArrayList)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getData() {
        firestoreDatabase.collection("Posts")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(requireActivity(), error.localizedMessage, Toast.LENGTH_LONG).show()
                } else {
                    if (value != null && !value.isEmpty) {
                        postArrayList.clear()
                        for (document in value.documents) {
                            val downloadUrl = document.getString("downloadUrl") ?: ""
                            val userEmail = document.getString("userEmail") ?: ""
                            val post = Post(userEmail, downloadUrl)
                            postArrayList.add(post)
                        }
                        feedAdapter.notifyDataSetChanged()
                    }
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.post_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_place -> {
                findNavController().navigate(R.id.action_postFragment_to_mapsFragment)
                true
            }
            R.id.signout -> {
                auth.signOut()
                sharedPreferences.edit().remove("remember_me").apply()
                findNavController().navigate(R.id.action_postFragment_to_loginFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        compositeDisposable.clear()
    }
}
