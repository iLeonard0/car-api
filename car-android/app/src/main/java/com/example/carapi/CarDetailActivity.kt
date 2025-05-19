package com.example.carapi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.carapi.model.Car
import com.example.carapi.service.Result
import com.example.carapi.service.RetrofitCar
import com.example.carapi.service.safeApiCall
import com.example.carapi.ui.loadUrl
import com.example.carapi.databinding.ActivityCarDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CarDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCarDetailBinding
    private lateinit var car: Car
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_car_detail)
        setContentView(binding.root)

        setupView()
        loadCar()
        setupGoogleMap()
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.deleteCTA.setOnClickListener {

            deleteCar()
        }

        binding.editCTA.setOnClickListener {
            editCar()
        }
    }

    private fun setupGoogleMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun editCar() {
        CoroutineScope(Dispatchers.IO).launch {
            val updatedCar = car.copy(licence = binding.license.text.toString())

            val result = safeApiCall {
                RetrofitCar.apiService.updateCar(car.id, updatedCar)
            }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@CarDetailActivity, R.string.unknown_error, Toast.LENGTH_SHORT
                        ).show()
                    }

                    is Result.Success -> {
                        Toast.makeText(
                            this@CarDetailActivity, R.string.success_update, Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this@CarDetailActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun loadCar() {
        val carId = intent.getStringExtra(ARG_ID) ?: ""
        Log.d("CarDetailActivity", "Car ID: $carId")

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitCar.apiService.getCarsId(carId) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Log.e("CarDetailActivity", "Error fetching car details: ${result.message}")
                    }

                    is Result.Success -> {
                        car = result.data
                        Log.d(
                            "CarDetailActivity", "Fetched car: $car"
                        )

                        handleSuccess()
                    }
                }
            }
        }
    }

    private fun deleteCar() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitCar.apiService.deleteCar(car.id) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@CarDetailActivity, R.string.error_delete, Toast.LENGTH_SHORT
                        ).show()
                    }

                    is Result.Success -> {
                        Toast.makeText(
                            this@CarDetailActivity, R.string.success_delete, Toast.LENGTH_SHORT
                        ).show()

                        finish()
                    }
                }
            }
        }
    }

    private fun handleSuccess() {
        binding.name.text = car.name
        binding.year.text = car.year
        binding.license.setText(car.licence)

        if (car.imageUrl.isNotEmpty()) {
            binding.image.loadUrl(car.imageUrl)
        } else {
            binding.image.setImageResource(R.drawable.ic_error)
        }
    }

    companion object {
        private const val ARG_ID = "ARG_ID"

        fun newIntent(
            context: Context, carId: String
        ) = Intent(context, CarDetailActivity::class.java).apply {
            putExtra(ARG_ID, carId)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (::car.isInitialized) {
            loadCarPlaceInGoogleMaps()
        }
    }

    private fun loadCarPlaceInGoogleMaps() {
        car.place?.let {
            binding.googleMapContent.visibility = View.VISIBLE
            val latLng = LatLng(it.lat, it.long)
            mMap.addMarker(
                MarkerOptions().position(latLng)
            )
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLng, 15f
                )
            )
        }
    }
}