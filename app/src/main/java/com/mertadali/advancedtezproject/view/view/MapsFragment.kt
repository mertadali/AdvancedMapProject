    package com.mertadali.advancedtezproject.view.view

    import android.Manifest
    import android.app.Activity.RESULT_CANCELED
    import android.app.Activity.RESULT_OK
    import android.content.Context
    import android.content.Intent
    import android.content.SharedPreferences
    import android.content.pm.PackageManager
    import android.location.Location
    import android.location.LocationListener
    import android.location.LocationManager
    import android.net.Uri
    import android.os.Build
    import android.os.Bundle
    import android.provider.MediaStore
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.activity.result.ActivityResultLauncher
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.core.app.ActivityCompat
    import androidx.core.content.ContextCompat
    import androidx.fragment.app.Fragment
    import androidx.navigation.fragment.findNavController
    import androidx.room.Room
    import com.google.android.gms.maps.CameraUpdateFactory
    import com.google.android.gms.maps.GoogleMap
    import com.google.android.gms.maps.OnMapReadyCallback
    import com.google.android.gms.maps.SupportMapFragment
    import com.google.android.gms.maps.model.LatLng
    import com.google.android.gms.maps.model.MarkerOptions
    import com.google.android.material.snackbar.Snackbar
    import com.google.firebase.Timestamp
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.ktx.auth
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.firestore.ktx.firestore
    import com.google.firebase.ktx.Firebase
    import com.google.firebase.storage.FirebaseStorage
    import com.google.firebase.storage.ktx.storage
    import com.mertadali.advancedtezproject.R
    import com.mertadali.advancedtezproject.databinding.FragmentMapsBinding
    import com.mertadali.advancedtezproject.view.model.Place
    import com.mertadali.advancedtezproject.view.roomdb.PlaceDao
    import com.mertadali.advancedtezproject.view.roomdb.PlaceDatabase
    import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
    import io.reactivex.rxjava3.disposables.CompositeDisposable
    import io.reactivex.rxjava3.schedulers.Schedulers
    import java.util.*

    class MapsFragment : Fragment(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

        private lateinit var locationManager: LocationManager
        private lateinit var locationListener: LocationListener
        private lateinit var mMap: GoogleMap
        private var _binding: FragmentMapsBinding? = null
        private val binding get() = _binding!!
        private lateinit var permissionLauncher : ActivityResultLauncher<String>
        private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
        private lateinit var sharedPreferences: SharedPreferences
        private var trackBoolean : Boolean? = null
        private var selectedLat : Double? = null
        private var selectedLon : Double? = null
        private lateinit var db : PlaceDatabase
        private lateinit var dao : PlaceDao
        private val compositeDisposable = CompositeDisposable()
        private var placeFromMaps : Place? = null
        private var selectedPicture : Uri? = null
        private lateinit var firestoreDatabase : FirebaseFirestore
        private lateinit var storage : FirebaseStorage
        private lateinit var auth : FirebaseAuth


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            registerLauncher()
            registerLauncherImage()
            sharedPreferences = requireActivity().getSharedPreferences("package com.mertadali.advancedtezproject",Context.MODE_PRIVATE)
            trackBoolean = false
            selectedLat = 0.0
            selectedLon = 0.0

            db = Room.databaseBuilder(requireContext(),PlaceDatabase::class.java,"Places")
                .build()
            dao = db.placeDao()
            auth = Firebase.auth
            firestoreDatabase = Firebase.firestore
            storage = Firebase.storage



        }


        override fun onMapReady(googleMap: GoogleMap){
            mMap = googleMap
            mMap.setOnMapLongClickListener(this)

                // Yeni yer ekleme işlemleri

            val info = arguments?.getString("info")
            if (info == "new"){
                binding.saveButton.visibility = View.VISIBLE
                binding.deleteButton.visibility = View.GONE
                locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

                locationListener = object : LocationListener{
                    override fun onLocationChanged(location: Location) {
                        trackBoolean = sharedPreferences.getBoolean("trackBoolean",false)
                        if (!trackBoolean!!){
                            val userLocation = LatLng(location.latitude,location.longitude)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))
                            sharedPreferences.edit().putBoolean("trackBoolean",true).apply()

                        }
                    }

                }
                //  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                // Permissions
                if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.ACCESS_FINE_LOCATION)){
                        Snackbar.make(binding.root,"Permission needed for location",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                            // permission request
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }.show()


                    }else{
                        // permission request
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }

                }else{
                    // permission granted - ContextCompat
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                    val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation!=null){
                        val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                    }
                    mMap.isMyLocationEnabled = true
                }

            }else{
                mMap.clear()
                placeFromMaps = arguments?.getSerializable("selectedPlace") as? Place
                placeFromMaps?.let {
                    val latLng = LatLng(it.latitude,it.longitude)
                    mMap.addMarker(MarkerOptions().position(latLng).title(it.name))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f))

                    binding.placeName.setText(it.name)
                    binding.saveButton.visibility = View.GONE
                    binding.deleteButton.visibility = View.VISIBLE
                }

            }

        }


        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            _binding = FragmentMapsBinding.inflate(inflater, container, false)
            val view = binding.root
            return view

        }
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            binding.saveButton.isEnabled = false

            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(this)

            binding.saveButton.setOnClickListener {
                save(it)
            }
            binding.deleteButton.setOnClickListener {

                delete(it)
            }

            binding.imageView.setOnClickListener {
                // Galeriye gitmek için izin gerekli
                if (Build.VERSION.SDK_INT == 28){
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                                Manifest.permission.READ_EXTERNAL_STORAGE)){
                            // permission request
                            Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give permission",
                                {
                                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                                }).show()
                        }else{
                            // permission request
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }

                    }else{
                        // permission granted - ContextCompat
                        val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        activityResultLauncher.launch(intentToGallery)
                    }
                }
            }
        }
        private fun registerLauncherImage(){
            activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
                if (result.resultCode == RESULT_OK){
                    val intentFromResult = result.data
                    if (intentFromResult != null){
                        selectedPicture =  intentFromResult.data
                        selectedPicture?.let {
                            binding.imageView.setImageURI(it)
                        }
                    }

                }else if (result.resultCode == RESULT_CANCELED){
                    Toast.makeText(requireContext(),"Permission needed for galllery",Toast.LENGTH_LONG).show()
                }
            }
            permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
                if (it){
                    // permission granted
                    val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                }
            }
        }
        private fun registerLauncher(){
            permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
                if (result){
                    // Permission granted
                    if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                        val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (lastLocation!=null){
                            val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                        }
                        mMap.isMyLocationEnabled = true

                    }

                }else{
                    // Permission denied
                    Toast.makeText(requireContext(),"Permission needed",Toast.LENGTH_LONG).show()
                }

            }
        }

        override fun onMapLongClick(p0: LatLng) {
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(p0))
            selectedLat = p0.latitude
            selectedLon = p0.longitude

            binding.saveButton.isEnabled = true
        }
        private fun save(view: View) {

            if (selectedLat != null && selectedLon != null) {

                val uuid = UUID.randomUUID()
                val imageName = "$uuid.jpg"

                val storage = Firebase.storage
                val reference = storage.reference
                val imageReference = reference.child("images").child(imageName)
                if (selectedPicture != null) {
                    imageReference.putFile(selectedPicture!!).addOnSuccessListener {
                        // download url alıp -> Firestore kaydetme işlemi
                        val uploadedImageReference = storage.reference.child("images").child(imageName)
                        uploadedImageReference.downloadUrl.addOnSuccessListener {
                            val downloadUrl = it.toString()

                            if (auth.currentUser != null) {
                                val postMap = hashMapOf<String, Any>()
                                postMap["downloadUrl"] = downloadUrl
                                postMap["userEmail"] = auth.currentUser!!.email!!
                                postMap["date"] = Timestamp.now()

                                firestoreDatabase.collection("Posts").add(postMap)
                                    .addOnCompleteListener {
                                        val place = Place(
                                            binding.placeName.text.toString(),
                                            binding.editTextDescription.text.toString(),
                                            selectedLat!!,
                                            selectedLon!!
                                        )
                                        compositeDisposable.add(
                                            dao.insert(place)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(this::handleResponse)
                                        )
                                        val action = MapsFragmentDirections.actionMapsFragmentToPostFragment()
                                        findNavController().navigate(action)

                                        println(action)
                                        //back

                                    }.addOnFailureListener {
                                        Toast.makeText(
                                            requireContext(),
                                            it.localizedMessage,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                            }
                        }.addOnFailureListener {
                            Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG)
                                .show()
                        }
                    }


                }
            }
        }

        private fun delete(view: View){
            placeFromMaps?.let {
                compositeDisposable.add(
                    dao.delete(it)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            // Delete işlemi başarıyla tamamlandığında bu blok çalışır
                            Toast.makeText(requireContext(), "Delete successful", Toast.LENGTH_SHORT).show()
                            handleResponse()
                        }, {
                            // Delete işlemi sırasında hata oluşursa bu blok çalışır
                            Toast.makeText(requireContext(), "Delete failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        })
                )
            }
        }
        private fun handleResponse() {
            try {
                val action = PostFragmentDirections.actionPostFragmentToMapsFragment()
                findNavController().navigate(action)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }




        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
            compositeDisposable.clear()
        }

    }