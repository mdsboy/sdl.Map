package jp.ac.titech.itpro.sdl.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var map: GoogleMap? = null
    private var infoView: TextView? = null
    private var locationClient: FusedLocationProviderClient? = null
    private var request: LocationRequest? = null
    private var callback: LocationCallback? = null
    private var currentLocButton: Button? = null
    private var lastLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)

        infoView = findViewById(R.id.info_view)
        val fragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        if (fragment != null) {
            Log.d(TAG, "onCreate: getMapAsync")
            fragment.getMapAsync(this)
        }
        locationClient = LocationServices.getFusedLocationProviderClient(this)

        request = LocationRequest.create()
        request?.let {
            it.interval = 10000L
            it.fastestInterval = 5000L
            it.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        currentLocButton = findViewById(R.id.button)
        currentLocButton!!.setOnClickListener {
            lastLocation?.let { ll ->
                infoView!!.text = getString(R.string.latlng_format, ll.latitude, ll.longitude)
                if (map == null) {
                    Log.d(TAG, "onLocationResult: map == null")
                } else {
                    map!!.animateCamera(CameraUpdateFactory.newLatLng(ll))
                }
            }
        }

        callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Log.d(TAG, "onLocationResult")
                val location = locationResult.lastLocation
                val ll = LatLng(location.latitude, location.longitude)

                if (lastLocation == null) {
                    infoView!!.text = getString(R.string.latlng_format, ll.latitude, ll.longitude)

                    if (map == null) {
                        Log.d(TAG, "onLocationResult: map == null")
                        return
                    }
                    map!!.animateCamera(CameraUpdateFactory.newLatLng(ll))
                }
                lastLocation = ll
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        startLocationUpdate(true)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        stopLocationUpdate()
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG, "onMapReady")
        map.moveCamera(CameraUpdateFactory.zoomTo(15f))
        this.map = map
    }

    private fun startLocationUpdate(reqPermission: Boolean) {
        Log.d(TAG, "startLocationUpdate")
        for (permission in PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                if (reqPermission) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS, REQ_PERMISSIONS)
                } else {
                    val text = getString(R.string.toast_requires_permission_format, permission)
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
        locationClient!!.requestLocationUpdates(request, callback, null)
    }

    override fun onRequestPermissionsResult(reqCode: Int, permissions: Array<String>, grants: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult")
        if (reqCode == REQ_PERMISSIONS) {
            startLocationUpdate(false)
        }
    }

    private fun stopLocationUpdate() {
        Log.d(TAG, "stopLocationUpdate")
        locationClient!!.removeLocationUpdates(callback)
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val PERMISSIONS = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        )
        private const val REQ_PERMISSIONS = 1234
    }
}
