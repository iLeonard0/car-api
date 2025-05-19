package com.example.carapi

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carapi.adapter.CarAdapter
import com.example.carapi.databinding.ActivityMainBinding
import com.example.carapi.model.Car
import com.example.carapi.service.Result
import com.example.carapi.service.RetrofitCar
import com.example.carapi.service.safeApiCall
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.carapi.database.DatabaseBuilder
import com.example.carapi.database.model.UserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestLocationPermission()
        setupView()
    }

    override fun onResume() {
        super.onResume()
        fetchItems()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                onLogout()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onLogout() {
        FirebaseAuth.getInstance().signOut()

        val intent = LoginActivity.newIntent(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }

    private fun setupView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            fetchItems()
        }

        binding.addCta.setOnClickListener {
            startActivity(NewCarActivity.newIntent(this))
        }

    }

    private fun fetchItems() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitCar.apiService.getCars() }

            withContext(Dispatchers.Main) {
                binding.swipeRefreshLayout.isRefreshing = false
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.error_fetching_data,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is Result.Success -> handleOnSuccess(result.data)
                }
            }
        }
    }

    private fun handleOnSuccess(data: List<Car>) {
        val adapter = CarAdapter(data) {
            startActivity(CarDetailActivity.newIntent(this, it.id))
        }

        binding.recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun requestLocationPermission() {
        // Inicializa o fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configura o launcher para solicitar a permissão do usuário
        locationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    getLastLocation()
                } else {
                    Toast.makeText(
                        this,
                        R.string.denied_permission,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        checkLocationPermissionAndRequest()
    }

    private fun checkLocationPermissionAndRequest() {
        when {
            checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        ACCESS_COARSE_LOCATION
                    ) == PERMISSION_GRANTED -> {
                getLastLocation()
            }

            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                locationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }

            shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION) -> {
                locationPermissionLauncher.launch(ACCESS_COARSE_LOCATION)
            }

            else -> {
                locationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getLastLocation() {
        if (checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED ||
            checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }

        fusedLocationClient.lastLocation.addOnCompleteListener { task: Task<Location> ->
            if (task.isSuccessful && task.result != null) {
                val location = task.result
                val userLocation =
                    UserLocation(latitude = location.latitude, longitude = location.longitude)

                Log.d(
                    "HELLO_WORLD",
                    "Lat: ${userLocation.latitude} Long: ${userLocation.longitude}"
                )

                CoroutineScope(Dispatchers.IO).launch {
                    DatabaseBuilder.getInstance()
                        .userLocationDao()
                        .insert(userLocation)
                }
            } else {
                Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}