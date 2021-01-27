package com.example.ktandroidtask.services

import android.app.*
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.*
import android.util.Log
import android.widget.Toast
import io.realm.kotlin.createObject
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.example.ktandroidtask.Home
import com.example.ktandroidtask.R
import com.example.ktandroidtask.RealmInterface.UserLocationModel
import com.example.ktandroidtask.Utils.Utils
import com.example.ktandroidtask.pojoModels.UpdateLocation
import com.example.ktandroidtask.pojoModels.UserLocation
import com.example.ktandroidtask.sharedPreference.SharedPreference
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_sing_up.*
import java.text.SimpleDateFormat
import java.util.*

class LocationUpdateService : Service() {

    private  var userEmailId: String?=null
    private lateinit var sharedPreference: SharedPreference
    private lateinit var userLocationModel: UserLocationModel
    lateinit var realm: Realm


    private val mBinder = LocalBinder()

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private var mChangingConfiguration = false

    private var mNotificationManager: NotificationManager? = null

    /**
     * Contains parameters used by [com.google.android.gms.location.FusedLocationProviderApi].
     */
    private var mLocationRequest: LocationRequest? = null

    /**
     * Provides access to the Fused Location Provider API.
     */
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Callback for changes in location.
     */
    private var mLocationCallback: LocationCallback? = null

    private var mServiceHandler: Handler? = null

    /**
     * The current location.
     */
    private var mLocation: Location? = null

    private lateinit var activityPendingIntent : PendingIntent
    /**
     * Returns the [NotificationCompat] used as part of the foreground service.
     */
    private// Extra to help us figure out if we arrived in onStartCommand via the notification or not.
    // The PendingIntent that leads to a call to onStartCommand() in this service.
    // The PendingIntent to launch activity.
    // Set the Channel ID for Android O.
    // Channel ID
    val notification: Notification
        get() {
            val intent = Intent(this, LocationUpdateService::class.java)
            // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
            intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)
            // The PendingIntent that leads to a call to onStartCommand() in this service.
            val servicePendingIntent = PendingIntent.getService(
                    this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )
            // The PendingIntent to launch activity.
                activityPendingIntent = PendingIntent.getActivity(
                        this, 0,
                        Intent(this, Home::class.java), 0
                )

            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
//                .addAction(
//                    R.drawable.ic_helmet_notification, getString(R.string.launch_activity),
//                    activityPendingIntent
//                )
//                .addAction(
//                    R.drawable.ic_close_black_24dp, getString(R.string.stop_tracking),
//                    servicePendingIntent
//                )
                    .setContentTitle(getString(R.string.app_name))
                    .setOngoing(true)
                    .setContentIntent(activityPendingIntent)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setWhen(System.currentTimeMillis())
            // Set the Channel ID for Android O.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(CHANNEL_ID) // Channel ID
            }

            return builder.build()
        }

    override fun onCreate() {

        sharedPreference = SharedPreference()

        realm  = Realm.getDefaultInstance();
        userLocationModel = UserLocationModel()


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult.lastLocation)
            }
        }

        createLocationRequest()
        getLastLocation()
        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel =
                    NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager!!.createNotificationChannel(mChannel)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service started")
        val startedFromNotification = intent.getBooleanExtra(
                EXTRA_STARTED_FROM_NOTIFICATION,
                false
        )

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates()
            stopSelf()
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return Service.START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mChangingConfiguration = true
    }

    override fun onBind(intent: Intent): IBinder? {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()")
        stopForeground(true)
        mChangingConfiguration = false
        userEmailId = sharedPreference.getValue(this, "userEmailId")
        return mBinder
    }

    override fun onRebind(intent: Intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()")
        stopForeground(true)
        mChangingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.i(TAG, "Last client unbound from service")
        if (userEmailId !=null) {
            userEmailId = sharedPreference.getValue(this, "userEmailId")
            Log.i(TAG, "LocationUpdate No userEmailId")
            return true
        }

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && Utils().requestingLocationUpdates(this)) {
            Log.i(TAG, "Starting foreground service")

            startForeground(NOTIFICATION_ID, notification)
        }
        return true // Ensures onRebind() is called when a client re-binds.
    }

    override fun onDestroy() {
        mServiceHandler?.removeCallbacksAndMessages(null)

    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * [SecurityException].
     */
    fun requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates")
        Utils().setRequestingLocationUpdates(this, true)
        startService(Intent(applicationContext, LocationUpdateService::class.java))
        try {
            mFusedLocationClient!!.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback!!, Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            Utils().setRequestingLocationUpdates(this, false)
            Log.e(TAG, "Lost location permission. Could not request updates. $unlikely")
        }

    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * [SecurityException].
     */
    fun removeLocationUpdates() {
        Log.i(TAG, "Removing location updates")
        try {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback!!)
            Utils().setRequestingLocationUpdates(this, false)
            stopSelf()
        } catch (unlikely: SecurityException) {
            Utils().setRequestingLocationUpdates(this, true)
            Log.e(TAG, "Lost location permission. Could not remove updates. $unlikely")
        }

    }

    private fun getLastLocation() {
        try {
            mFusedLocationClient!!.lastLocation
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful && task.result != null) {
                            mLocation = task.result
                        } else {
                            Log.w(TAG, "Failed to get location.")
                        }
                    }
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }

    }

    private fun onNewLocation(location: Location) {
        Log.i(TAG, "New location: $location")

        mLocation = location


        updateLocation(mLocation!!)

        // Notify anyone listening for broadcasts about the new location.
        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_LOCATION, location)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager?.notify(NOTIFICATION_ID, notification)
        }
    }

    /**
     * Sets the location request parameters.
     */
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun updateLocation(location:Location) {


        val userEmailId = sharedPreference.getValue(this,"userEmailId")

        if(userEmailId == null){
            Log.i("LocationUpdateService", "updateLocation (line 311): No userEmailId")
            return
        }

        Log.i("LocationUpdateService", "updateLocation (line 318):updateLocationReq ${userEmailId} location ${location}")

        try {
            realm.beginTransaction()
            val updateLocation = realm.createObject<UpdateLocation>()
            updateLocation.latitude =location.latitude
            updateLocation.longitude=location.longitude
            val sdf = SimpleDateFormat("DD/MMM/YYYY hh:mm")
            val currentDate = sdf.format(Date())
            Log.i("LocationUpdateService", "updateLocation: current date $currentDate")
            updateLocation.date = currentDate

            realm.commitTransaction()

            val user = UserLocation(
                    email = userEmailId,
                    location =updateLocation

            )
            val response = userLocationModel.updateUserLocation(realm, user)
            if(response){
                Log.i("LocationUpdateService", "updateLocation (line 326): Location updated.")
                val response = userLocationModel.getAllUserLocation(realm,userEmailId.toString())
                Log.i("LocationUpdateService", "updateLocation (line 326): Location updated. response ${response?.count()}")


            }else{
                Log.i("LocationUpdateService", "updateLocation (line 326): No location updated.")
            }

        } catch (e: Exception) {
            Log.i("signUp", "signUp: RealmPrimaryKeyConstraintException ${e.message}")
            Toast.makeText(applicationContext, "Primary Key exists, Press Update instead", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        internal val service: LocationUpdateService
            get() = this@LocationUpdateService
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The [Context].
     */
    private fun serviceIsRunningInForeground(context: Context): Boolean {
        val manager = context.getSystemService(
                Context.ACTIVITY_SERVICE
        ) as ActivityManager
        for (service in manager.getRunningServices(
                Integer.MAX_VALUE
        )) {
            if (javaClass.name == service.service.className) {
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }

    companion object {

        private val PACKAGE_NAME =
                "com.example.ktandroidtask"

        private val TAG = LocationUpdateService::class.java.simpleName

        /**
         * The name of the channel for notifications.
         */
        private val CHANNEL_ID = "channel_01"

        internal val ACTION_BROADCAST = "$PACKAGE_NAME.broadcast"

        internal val EXTRA_LOCATION = "$PACKAGE_NAME.location"
        private val EXTRA_STARTED_FROM_NOTIFICATION = "$PACKAGE_NAME.started_from_notification"

        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000

        /**
         * The fastest rate for active location updates. Updates will never be more frequent
         * than this value.
         */
        private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

        /**
         * The identifier for the notification displayed for the foreground service.
         */
        private val NOTIFICATION_ID = 12345678
    }


}


