package com.example.ktandroidtask

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ktandroidtask.RealmInterface.UserLocationModel
import com.example.ktandroidtask.pojoModels.UpdateLocation
import com.example.ktandroidtask.pojoModels.UserLocation
import com.example.ktandroidtask.services.LocationUpdateService
import com.example.ktandroidtask.sharedPreference.SharedPreference
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.maps.android.SphericalUtil
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import io.realm.Realm
import io.realm.kotlin.createObject
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_sing_up.*
import kotlinx.android.synthetic.main.location_bottom_sheet.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class Home : AppCompatActivity(),
        View.OnClickListener,
        GoogleMap.OnMarkerClickListener,
        OnMapReadyCallback {

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 15000
    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2


    // Monitors the state of the connection to the service.
    private val locationServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationUpdateService.LocalBinder
            locationService = binder.service
            islocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationService = null
            islocationServiceBound = false
        }
    }

    @SuppressLint("ServiceCast")
    fun isLocationEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lm = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isLocationEnabled
        } else {
            val mode = Settings.Secure.getInt(
                    this.contentResolver, Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

    private lateinit var googleMap: GoogleMap
    private lateinit var mapMarker: Marker
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userLocation: LatLng
    private var islocationServiceBound = false
    private var locationService: LocationUpdateService? = null
    private var shouldStartGettingLocationUpdates: Boolean? = true
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback
    private  var userEmailId: String?=null
    private lateinit var sharedPreference: SharedPreference
    private lateinit var userLocationModel: UserLocationModel
    lateinit var realm: Realm
    private lateinit var locationBottomSheetBehavior: BottomSheetBehavior<*>
    private var isShowLiveTracking: Boolean? = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this@Home)

        initRecyclerView()
        setButtonListener()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPreference = SharedPreference()
        realm  = Realm.getDefaultInstance();
        userLocationModel = UserLocationModel()
        userEmailId = sharedPreference.getValue(this, "userEmailId")

        val bottomSheet = findViewById<View>(R.id.bottom_sheet)
        locationBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        setupHomeBottomSheet()
        getAllLocation()
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                userLocation = LatLng(
                        locationResult.lastLocation.latitude,
                        locationResult.lastLocation.longitude
                )
                    Log.i("Home", "onLocationResult (line 202): email is ${userEmailId}")
                    if(isShowLiveTracking == true){
                        showTruckingMarker()
                    }
                    getAllLocation()


            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                super.onLocationAvailability(locationAvailability)
                Log.d("Home", "onLocationAvailability: $locationAvailability")

                if (locationAvailability.isLocationAvailable) {

                } else {

                    if (!isLocationEnabled()) {
                        val bundle = Bundle()
//                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Home screen")
//                        mFirebaseAnalytics.logEvent("NO_GPS", bundle)

//                        val intent = Intent(applicationContext, GpsPrompt::class.java)
//                        startActivityForResult(intent, 1)
                        Log.d("Home", "onCreate: Location Services Is Disabled")
                    }
                }

            }
        }
    }
    fun initRecyclerView() {
        location_recyclerView.layoutManager = LinearLayoutManager(this)
    }

    fun showTruckingMarker(){
        if(userEmailId !=null){
            Log.i("Home", "showTruckingMarker: user email ${userEmailId}")
           val response = userLocationModel.getUserLocation(realm, userEmailId.toString())
            Log.i("Home", "showTruckingMarker: response ${response}")
            if (response != null) {
                val updateLocation = LatLng(response.location!!.latitude!!, response.location!!.longitude!!)
                animateMarker(mapMarker.position, updateLocation, mapMarker)
                mapMarker.isVisible = true

            }
        }
    }

    fun getAllLocation(){
        val response = userEmailId?.let { userLocationModel.getAllUserLocation(realm, it) }
        if (response != null) {
            Log.i("Home", "getAllLocation:response ${response.count()} ")
        }
        if (response != null) {
            if(response.isNotEmpty()){
                tv_no_location.visibility=GONE
                location_recyclerView.visibility=VISIBLE
                val adapter =  UserLocationAdapter(response,this);
                location_recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()

            }else{
                tv_no_location.visibility=VISIBLE
                location_recyclerView.visibility=GONE
            }
        }else{
            tv_no_location.visibility=VISIBLE
            location_recyclerView.visibility=GONE
        }
    }

    private fun animateMarker(startPosition: LatLng, nextPosition: LatLng, mMarker: Marker) {
        val handler = Handler()
        val start = SystemClock.uptimeMillis()
        val interpolator = AccelerateDecelerateInterpolator()
        val durationInMs = 3000f
        val hideMarker = false


        val dist = SphericalUtil.computeDistanceBetween(startPosition, nextPosition)

        if (dist < 30) {
            return
        }

        handler.post(object : Runnable {
            internal var elapsed: Long = 0
            internal var t: Float = 0.toFloat()
            internal var v: Float = 0.toFloat()

            override fun run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start
                t = elapsed / durationInMs
                v = interpolator.getInterpolation(t)
                val currentPosition = LatLng(
                        startPosition.latitude * (1 - t) + nextPosition.latitude * t,
                        startPosition.longitude * (1 - t) + nextPosition.longitude * t
                )
                mMarker.setPosition(currentPosition)
                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                } else {
                    mMarker.isVisible = !hideMarker
                }
            }
        })

    }

    override fun onMapReady(p0: GoogleMap) {

        p0.setOnMarkerClickListener(this)
        p0.uiSettings.isCompassEnabled = false
        p0.uiSettings.isMyLocationButtonEnabled = false
        googleMap = p0

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setDeniedMessage("Please enable your location permissions to continue using KT android task.You can turn on permissions in  [App Settings] > [Permission]\")")
                .setPermissions(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION

                )
                .check();


    }

    private var permissionListener = object : PermissionListener {

        override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
            Log.i("Home", "Permission was denied")
        }

        @SuppressLint("MissingPermission")
        override fun onPermissionGranted() {
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location?.latitude != null) {
                            userLocation = LatLng(location.latitude, location.longitude)
                            googleMap.isMyLocationEnabled = true
                            mapMarker = googleMap.addMarker(
                                    MarkerOptions().position(userLocation).visible(false)
                            )
                            mapMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.custom_marker1))
                            moveCameraToUser()
                            addUserLocation(location.latitude, location.longitude)

                        } else {
                            Log.i(
                                    "Home",
                                    "onPermissionGranted (line 125): Unable to fetch user location"
                            )
                        }
                    }
        }
    }

    fun setButtonListener(){
        fab.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            fab ->{
                mapMarker.hideInfoWindow()
                isShowLiveTracking= true
                moveCameraToUser()
                showTruckingMarker()
            }
        }
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        mapMarker.isVisible =false
        locationBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        return true
    }

    fun moveCameraToUser() {

        Log.i("Home", "moveCameraToUser (line 81): ")
        try {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLocation, 15f);
            googleMap.animateCamera(cameraUpdate);
        } catch (ex: Exception) {
            Log.i("Home", "moveCameraToUser (line 104): Location is null. ")
            ex.printStackTrace()

        }

    }


    fun showMarker(email:String,latLng: LatLng) {

        Log.i("Home", "showMarker (line 81): ")
        try {
            locationBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            val geoCoder = Geocoder(this)
                val addresses = geoCoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1
                )

                if (addresses.size == 0) {
                    Log.i("Home", "showMarker (line 1168): No address")
                    return
                }
                val addressFromApi = addresses[0].getAddressLine(0)
                val city = addresses.get(0).locality
                if (addressFromApi != null && city != null) {
                    Log.i("Home", "showMarker:$latLng address:$addressFromApi")
                    mapMarker.isVisible=true
                    mapMarker.snippet = "Location: ${city}"
                    mapMarker.title = "Email: ${email}"
                    mapMarker.showInfoWindow();
                    googleMap.setInfoWindowAdapter(CustomInfoWindowForGoogleMap(this))
                    animateMarker(mapMarker.position, latLng, mapMarker)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f);
                    googleMap.animateCamera(cameraUpdate);

                }

        } catch (ex: Exception) {
            Log.i("Home", "showMarker (line 104): Location is null.")
            ex.printStackTrace()

        }

    }
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create()
        mLocationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }


    fun addUserLocation(lat:Double,long:Double){
        Log.i("Home", "addUserLocation: addUserLocation ${userEmailId}")
        if (userEmailId !=null){
            try {
                mapMarker.hideInfoWindow()
                realm.beginTransaction()
                val updateLocation = realm.createObject<UpdateLocation>()
                updateLocation.email = userEmailId.toString()
                updateLocation.latitude =lat
                updateLocation.longitude=long
                val sdf = SimpleDateFormat("MMM DD YYYY hh:mm")
                val currentDate = sdf.format(Date())
                Log.i("Home", "updateLocation: current date $currentDate")
                updateLocation.date = currentDate
                realm.commitTransaction()
                val user = UserLocation(
                        email = userEmailId.toString(),
                        location = updateLocation

                )
                val response = userLocationModel.addUserLocation(realm, user)
                if(response){
                    Log.i("Home", "addUserLocation response ${response}")

                    locationService?.requestLocationUpdates()

                }else{
                    if (::fusedLocationClient.isInitialized) {
                        fusedLocationClient.removeLocationUpdates(mLocationCallback)
                    }
                    locationService?.removeLocationUpdates()
                    Log.i("Home", "addUserLocation response error ${response}")

                }

            } catch (e: Exception) {
                Log.i("Home", "addUserLocation: response error ${e.message}")
                if (::fusedLocationClient.isInitialized) {
                    fusedLocationClient.removeLocationUpdates(mLocationCallback)
                }
                locationService?.removeLocationUpdates()
            }
        }else{
            Log.i("Home", "addUserLocation No userEmailId")

        }

    }

    private fun setupHomeBottomSheet() {

        locationBottomSheetBehavior.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // React to state change
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        fab.hide()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        fab.show()
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        fab.show()
                    }
                    else -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // React to dragging events
            }
        })

    }
    override fun onStart() {
        super.onStart()

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(
                Intent(this, LocationUpdateService::class.java), locationServiceConnection,
                Context.BIND_AUTO_CREATE
        )
    }

    //    override fun onPause() {
//        super.onPause()
//
//        if (::fusedLocationClient.isInitialized) {
//            fusedLocationClient.removeLocationUpdates(mLocationCallback)
//        }
//
//    }

    override fun onStop() {
        if (islocationServiceBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(locationServiceConnection)
            islocationServiceBound = false
        }
        super.onStop()
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()

        userEmailId = sharedPreference.getValue(this, "userEmailId")

        val mshouldStartGettingLocationUpdates = shouldStartGettingLocationUpdates
        if (mshouldStartGettingLocationUpdates != null){
            if (mshouldStartGettingLocationUpdates) {
                locationService?.requestLocationUpdates()
//                setSwitchState(mshouldStartGettingLocationUpdates)
            }
        }

        createLocationRequest()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationService?.requestLocationUpdates()
        fusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.myLooper())
    }
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}