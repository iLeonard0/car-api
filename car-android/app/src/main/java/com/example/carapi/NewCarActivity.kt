package com.example.carapi

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.FileProvider
import com.example.carapi.model.Car
import com.example.carapi.model.Place
import com.example.carapi.service.Result
import com.example.carapi.service.RetrofitCar
import com.example.carapi.service.safeApiCall
import com.example.carapi.databinding.ActivityNewCarBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import java.security.SecureRandom

class NewCarActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityNewCarBinding
    private lateinit var imageUri: Uri
    private var imageFile: File? = null
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedMarker: Marker? = null

    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            binding.imageUrl.setText("Imagem Obtida")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupGoogleMap()
        requestPermissions()
        setupView()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        binding.mapContent.visibility = View.VISIBLE
        getDeviceLocation()
        mMap.setOnMapClickListener { latLng: LatLng ->

            selectedMarker?.remove()

            selectedMarker = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title("Lat: ${latLng.latitude}, Long: ${latLng.longitude}")
            )
        }
    }

    private fun setupGoogleMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getDeviceLocation() {
       if (checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
           loadCurrentLocation()
       } else {
           requestPermissions(
               this,
               arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
               LOCATION_PERMISSION_REQUEST_CODE
           )
       }
    }

    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation() {
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLocation = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
            } else {
                Toast.makeText(
                    this,
                    "Não foi possível obter a localização atual. Verifique suas configurações de localização.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(
                this,
                "Erro ao obter localização: ${exception.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    "Permissão de Localização negada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.saveCta.setOnClickListener {
            save()
        }

        binding.takePictureCta.setOnClickListener {
            takePicture()
        }
    }

    private fun requestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(android.Manifest.permission.CAMERA)
        }

        if (checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                CAMERA_REQUEST_CODE
            )
        }
    }

    private fun takePicture() {
        if (checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            requestPermissions()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        imageUri = createImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        cameraLauncher.launch(intent)
    }

    private fun createImageUri(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

        return FileProvider.getUriForFile(
            this,
            "com.example.carapi.fileprovider",
            imageFile!!
        )
    }


    private fun uploadImageToFirebase() {

        val storageRef = FirebaseStorage.getInstance().reference

        val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

        val baos = ByteArrayOutputStream()

        val imageBitmap = BitmapFactory.decodeFile(imageFile!!.path)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val data = baos.toByteArray()

        binding.loadImageProgress.visibility = View.VISIBLE
        binding.takePictureCta.isEnabled = false
        binding.saveCta.isEnabled = false

        imagesRef.putBytes(data)
            .addOnFailureListener {
                binding.loadImageProgress.visibility = View.GONE
                binding.takePictureCta.isEnabled = true
                binding.saveCta.isEnabled = true
                Toast.makeText(this, "Falha ao realizar o upload", Toast.LENGTH_SHORT).show()
            }

            .addOnSuccessListener {
                binding.loadImageProgress.visibility = View.GONE
                binding.takePictureCta.isEnabled = true
                binding.saveCta.isEnabled = true
                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                    saveData(uri.toString())
                }
            }
    }


    private fun save() {
        if (!validateForm()) return
        uploadImageToFirebase()
    }

    private fun saveData(imageUrl: String) {

        val carPlace = selectedMarker?.position?.let {
            Place(
                it.latitude,
                it.longitude
            )
        }

        CoroutineScope(Dispatchers.IO).launch {
            val id = SecureRandom().nextInt().toString()

            val carValue = Car(
                id,
                imageUrl,
                binding.year.text.toString(),
                binding.name.text.toString(),
                binding.license.text.toString(),
                place = carPlace
            )

            val result = safeApiCall { RetrofitCar.apiService.addCar(carValue) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@NewCarActivity,
                            R.string.error_create,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is Result.Success -> {
                        Toast.makeText(
                            this@NewCarActivity,
                            getString(R.string.success_create, result.data.id),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        val fieldValidations = listOf(
            Pair(binding.name.text.toString(), getString(R.string.error_validate_form, "Nome")),
            Pair(binding.year.text.toString(), getString(R.string.error_validate_form, "Ano")),
            Pair(binding.license.text.toString(), getString(R.string.error_validate_form, "Placa"))
        )

        for ((value, errorMessage) in fieldValidations) {
            if (value.isBlank()) {
                showToast(errorMessage)
                return false
            }
        }

        if (imageFile == null) {
            showToast(getString(R.string.error_validate_take_picture))
            return false
        }

        if (selectedMarker == null) {
            showToast(getString(R.string.error_validate_location))
            return false
        }


        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 101
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
        
        fun newIntent(context: Context) =
            Intent(context, NewCarActivity::class.java)
    }
}
