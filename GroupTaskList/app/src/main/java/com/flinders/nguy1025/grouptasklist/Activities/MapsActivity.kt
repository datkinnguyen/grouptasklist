package com.flinders.nguy1025.grouptasklist.Activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.flinders.nguy1025.grouptasklist.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.maps_activity.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapClickListener {

    companion object {
        val coordinateKey = "coordinate"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastMarker: Marker? = null
    set(value) {field = value; updateCoordinateText()}

    private var lastLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maps_activity)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btn_done.setOnClickListener { v ->
            // pass last selected coordinate back to Task detail screen
            if (lastLocation != null) {
                var data = Intent()

                var coord = DoubleArray(2)
                coord[0] = lastLocation!!.latitude
                coord[1] = lastLocation!!.longitude

                data.putExtra(coordinateKey, coord)

                setResult(Activity.RESULT_OK, data)
            } else {
                setResult(Activity.RESULT_CANCELED, null)
            }

            // finish this activity to show Task Detail fragment screen
            finish()
        }

        // Load passing coordinate if any
        var coord = savedInstanceState?.getDoubleArray(coordinateKey)
        if (coord != null && coord.size == 2) {
            lastLocation = LatLng(coord[0], coord[1])
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        setUpMap()
    }

    override fun onMarkerDrag(p0: Marker?) {
        lastLocation = p0?.position
    }

    override fun onMarkerDragStart(p0: Marker?) {
        lastLocation = p0?.position
    }

    override fun onMarkerDragEnd(p0: Marker?) {
        lastLocation = p0?.position
    }

    override fun onMapClick(p0: LatLng?) {
        // remove all marker and add marker here
        if (p0 != null) {
            lastLocation = p0
            addMarkerToMap(p0)
        }
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        mMap.isMyLocationEnabled = true
        mMap.setOnMapClickListener(this)

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null && lastLocation == null) {
                lastLocation = LatLng(location.latitude, location.longitude)
            }

            val currentLatLng = LatLng(location.latitude, location.longitude)
            addMarkerToMap(currentLatLng)
        }
    }

    private fun markerFromLatLng(latLng: LatLng): MarkerOptions {
        return MarkerOptions().position(latLng).draggable(true).snippet("Coordinate: $latLng")

    }

    private fun addMarkerToMap(latLng: LatLng) {
        // remove existing marker
        if (lastMarker != null) {
            lastMarker?.remove()
        }

        lastMarker = mMap.addMarker(markerFromLatLng(latLng))
        val cu = CameraUpdateFactory.newLatLngZoom(latLng, 12f)
        mMap.animateCamera(cu)

    }

    private fun updateCoordinateText() {
        if (lastLocation != null) {
            tv_coordinates.text = lastLocation.toString()
        }
    }

}
