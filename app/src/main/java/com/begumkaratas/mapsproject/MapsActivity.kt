package com.begumkaratas.mapsproject

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.begumkaratas.mapsproject.databinding.ActivityMapsBinding

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    var takipBoolean: Boolean? = null
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        registerLauncher()

        sharedPreferences = getSharedPreferences("com.begumkaratas.mapsproject", MODE_PRIVATE)
        takipBoolean = false

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapLongClickListener(this)

        //39.939681, 32.623645
        /*   val evim=LatLng(39.939681,  32.623645)
           mMap.addMarker(MarkerOptions().position(evim).title("Korkut Ata"))
           mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(evim,10f))*

         */
        // Add a marker in Sydney and move the camera
        /*val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                takipBoolean = sharedPreferences.getBoolean("takipBoolean", false)
                if (!takipBoolean!!) {
                    mMap.clear()
                    val userLocation = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(MarkerOptions().position(userLocation).title("Konum!"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10f))
                    sharedPreferences.edit().putBoolean("takipBoolean", true).apply()

                }

            }

        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Snackbar.make(
                    binding.root,
                    "Konumu almak için izin gerekli",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(
                    "İzin ver"
                ) {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

            }
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0F,
                locationListener
            )
            val lastKnownLocation =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null) {
                val lastKnownLatlng =
                    LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatlng, 14f))
            }

        }
    }

    private fun registerLauncher() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    if (ContextCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0F, locationListener)
                        val lastKnownLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                  if (lastKnownLocation!=null) {
                      val lastKnownLatLng=LatLng(lastKnownLocation.latitude,lastKnownLocation.longitude)
                      mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng,14f))

                    }
                }
                Toast.makeText(this@MapsActivity, "izne ihtiyaç var", Toast.LENGTH_LONG).show()

            }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onMapLongClick(p0: LatLng) {
        mMap.clear()
        /* println(p0.latitude)
           println(p0.longitude)*/

        //geocoder
        val geocoder = Geocoder(this, Locale.getDefault())
        var adress = ""
        try {
            geocoder.getFromLocation(
                p0.latitude,
                p0.longitude,
                1,
                Geocoder.GeocodeListener { addressList ->
                    val firstAddress = addressList.first()
                    val ulkeName = firstAddress.countryName
                    val street = firstAddress.thoroughfare
                    val sokak = firstAddress.subThoroughfare
                    adress += street
                    adress += sokak

                    println(adress)

                })


        } catch (e: Exception) {
            e.printStackTrace()
        }
        mMap.addMarker(MarkerOptions().position(p0))
    }
}