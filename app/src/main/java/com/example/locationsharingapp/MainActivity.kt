package com.example.locationsharingapp

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.locationsharingapp.services.HttpClient
import org.json.JSONArray
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var currentMarker: Marker? = null
    private var httpClient: HttpClient = HttpClient()
    private var currentLocation: LatLng = LatLng(Double.NaN, Double.NaN)
    private var circle: Circle? = null
    private var animators: ArrayList<ValueAnimator> = ArrayList()
    private var animatorCircles: ArrayList<Circle> = ArrayList()
    private var usersMarkers: ArrayList<Marker> = ArrayList()

    private var searchBtnMode: String = "find"
    private var SEARCH_MODE_FIND: String = "find"
    private var SEARCH_MODE_CANCEL: String = "cancel"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        toolbar.title = "FEX - Educación"
        setSupportActionBar(toolbar)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment: SupportMapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val searchButton: Button = findViewById(R.id.btn_search_users)

        searchButton.text = if (searchBtnMode == SEARCH_MODE_FIND) {
            "Buscar"
        } else {
            "Cancelar"
        }

        searchButton.setOnClickListener {
            searchButtonAction()

            searchButton.text = if (searchBtnMode == SEARCH_MODE_FIND) {
                "Buscar"
            } else {
                "Cancelar"
            }
        }

        val deleteMarker: Button = findViewById(R.id.delete_marker)

        if (currentMarker == null) {
            deleteMarker.visibility = View.GONE
        }

        deleteMarker.setOnClickListener {
            currentMarker?.remove()
            deleteMarker.visibility = View.GONE
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }
        mMap?.isMyLocationEnabled = true
        fusedLocationClient?.lastLocation
            ?.addOnSuccessListener(this) { location ->
                if (location != null) {
                    currentLocation = LatLng(location.latitude, location.longitude)
                    mMap?.addMarker(
                        MarkerOptions().position(currentLocation).title(
                            "Ubicación actual: Longitud: " + location.longitude
                                    + " Latitud: " + location.latitude
                        )
                    )
                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                }
            }

        mMap?.setOnMapClickListener { latLng ->
            currentMarker?.remove()

            currentMarker = mMap?.addMarker(
                MarkerOptions().position(latLng).title(
                    "GPS: Longitud: " + latLng.longitude + " Latitud: " + latLng.latitude
                ).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

            val deleteMarker: Button = findViewById(R.id.delete_marker)
            deleteMarker.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap?.let { onMapReady(it) }
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchButtonAction() {
        if (searchBtnMode == SEARCH_MODE_FIND) {
            searchNearbyUsers()
            createSearchRange()
            searchBtnMode = SEARCH_MODE_CANCEL
        } else {
            cancelSearch()
            searchBtnMode = SEARCH_MODE_FIND
        }
    }

    private fun cancelSearch() {

        for (animator: Animator in animators) {
            animator.end()
            animator.removeAllListeners()
        }

        for (circle: Circle in animatorCircles) {
            circle.remove()
        }

        circle?.remove()
        circle = null

        for ( userMarker: Marker in usersMarkers) {
            userMarker.remove()
        }

        usersMarkers = ArrayList()
    }

    private fun searchNearbyUsers() {
        Thread {
            try {
                val response = httpClient.doGetRequest(
                    "https://eduar.free.beeceptor.com/todos?lon="
                            + currentLocation.longitude
                            + "&lat="
                            + currentLocation.latitude
                )
                runOnUiThread {
                    if (response != null) {
                        val jsonArray = JSONArray(response)

                        for (i in 0 until jsonArray.length()) {
                            val person = jsonArray.optJSONObject(i)
                            if (person != null) {
                                val username = person.optString("name", "Sin nombre")
                                val lat = person.optDouble("lat", Double.NaN)
                                val lon = person.optDouble("lon", Double.NaN)

                                if (!lat.isNaN() && !lon.isNaN()) {
                                    val userLocation = LatLng(lat, lon)
                                    val userMarker = mMap?.addMarker(
                                        MarkerOptions().position(userLocation).title(
                                            "Username: $username"
                                        )
                                            .icon(
                                                BitmapDescriptorFactory
                                                    .defaultMarker(
                                                        BitmapDescriptorFactory.HUE_MAGENTA
                                                    )
                                            )
                                    )

                                    if (userMarker != null) {
                                        usersMarkers.add(userMarker)
                                    }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Error en la petición", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun createSearchRange() {
        circle = mMap?.addCircle(
            CircleOptions()
                .center(currentLocation)
                .radius(1000.0)
                .strokeColor(Color.argb(90, 0, 0, 50))
                .fillColor(Color.argb(70, 0, 0, 50))
                .strokeWidth(5f)
        )

        createSearchRangeAnimation(
            location = currentLocation,
            range = 1000.0f,
        )
        createSearchRangeAnimation(
            location = currentLocation,
            range = 1000.0f,
            delay = 2000
        )
        createSearchRangeAnimation(
            location = currentLocation,
            range = 1000.0f,
            delay = 4000
        )
    }

    private fun createSearchRangeAnimation(
        location: LatLng,
        color: Int = Color.argb(50, 0, 0, 128),
        range: Float,
        delay: Long = 0,
        duration: Long = 6000,
        fill: Int = Color.argb(0, 0, 0, 255),
    ) {
        val circle = mMap?.addCircle(
            CircleOptions()
                .center(location)
                .radius(10.0)
                .strokeColor(color)
                .fillColor(fill)
                .strokeWidth(5f)
        )

        val animator = ValueAnimator.ofFloat(10f, range)
        animator.duration = duration
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.RESTART
        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            circle?.radius = animatedValue.toDouble()
        }

        animator.startDelay = delay
        animator.start()

        animators.add(animator)
        if (circle != null) {
            animatorCircles.add(circle)
        }
    }
}
