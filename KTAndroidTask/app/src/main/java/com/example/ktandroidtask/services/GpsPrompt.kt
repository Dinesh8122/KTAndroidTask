package com.example.ktandroidtask.services

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ktandroidtask.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.gun0912.tedpermission.PermissionListener
import kotlinx.android.synthetic.main.activity_enable_gps.*


class GpsPrompt : AppCompatActivity(), View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{


    override fun onConnected(p0: Bundle?) {


    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onClick(v: View?) {

        when(v) {
            enableGPS -> {
                mGoogleApiClient = GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this).build()
                mGoogleApiClient.connect()

                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                finish()
                Log.i("GpsPrompt","Enabling GPS")
            }
        }
    }


    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var locationPermissionListener: PermissionListener
//    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enable_gps)


        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if(gpsStatus)
        {
            Log.d("GpsPrompt", "GPS IS ON")

            val intent = Intent()
            intent.putExtra("isGPSOn", true)
            setResult(Activity.RESULT_OK, intent)
            finish()

        }
        else {

            locationPermissionListener =
                    object : PermissionListener {
                        override fun onPermissionGranted() {
                            Log.i("GpsPrompt", "onPermissionGranted (line 51): ")
                            val intent = Intent()
                            intent.putExtra("isGPSOn", true)
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }

                        override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {
                            Log.i("GpsPrompt", "onPermissionDenied (line 59): ")
                            Toast.makeText(this@GpsPrompt,"Please allow Location access to continue",Toast.LENGTH_SHORT).show()
                        }
                    }

            enableGPS.setOnClickListener(this)


            Log.d("GpsPrompt", "GPS IS OFF")
        }







    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}