package com.example.trailit.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.trailit.R
import com.example.trailit.other.Constants.ACTION_IDLE_SERVICE
import com.example.trailit.other.Constants.ACTION_PAUSE_SERVICE
import com.example.trailit.other.Constants.ACTION_START_RESUME_TRACKING_SERVICE
import com.example.trailit.other.Constants.ACTION_START_SERVICE
import com.example.trailit.other.Constants.ACTION_STOP_SERVICE
import com.example.trailit.other.Constants.CHANNEL_ID
import com.example.trailit.other.Constants.CHANNEL_NAME
import com.example.trailit.other.Constants.HIGHEST_UPDATE_INTERVAL
import com.example.trailit.other.Constants.LOWEST_UPDATE_INTERVAL
import com.example.trailit.other.Constants.NOTIFICATION_ID
import com.example.trailit.other.Constants.TIMER_UPDATE_INTERVAL
import com.example.trailit.other.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


typealias TrailLine = MutableList<LatLng>
typealias TrailLines = MutableList<TrailLine>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    private var map: GoogleMap? = null


    var isFistTrail = true
    var serviceKilled = false

    private lateinit var currentLocation: Location


    //needed to provide location updates.
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val trailTimeInSeconds = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var realTimeNotificationBuilder: NotificationCompat.Builder

    companion object {
        val trailTimeInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val coordinatePoints = MutableLiveData<TrailLines>()

    }

    private fun createInitialValues() {
        isTracking.postValue(false)
        coordinatePoints.postValue(mutableListOf())
        trailTimeInSeconds.postValue(0L)
        trailTimeInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        realTimeNotificationBuilder = baseNotificationBuilder
        createInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        isTracking.observe(this, Observer {
            updateTracking(it)
            updateNotificationMappingState(it)
        })

    }

    private fun killService() {
        serviceKilled = true
        isFistTrail = true
        pauseService()
        createInitialValues()
        stopForeground(true)
        stopSelf()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {

                ACTION_START_SERVICE -> {
                    startLocationForegroundService()
                }
                ACTION_START_RESUME_TRACKING_SERVICE -> {
                    if (isFistTrail) {
                        startForegroundService()
                        isFistTrail = false
                    } else {
                        Timber.d("Resuming service...")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }

                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService()
                }

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    //longitude & latitude
    private fun addCoordinatePoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            coordinatePoints.value?.apply {
                last().add(pos)
                coordinatePoints.postValue(this)
            }
        }
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var trailTime = 0L
    private var startTime = 0L
    private var finalSecondTimeStamp = 0L

    private fun startTimer() {
        addEmptyLine()
        isTracking.postValue(true)
        startTime = System.currentTimeMillis()
        isTimerEnabled = true
        //don't execute all at once, for better performance, coroutine will cause a few second delay.
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - startTime

                trailTimeInMillis.postValue(trailTime + lapTime)
                if (trailTimeInMillis.value!! >= finalSecondTimeStamp + 1000L) {
                    trailTimeInSeconds.postValue(trailTimeInSeconds.value!! + 1)
                    finalSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            trailTime += lapTime
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun updateNotificationMappingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_RESUME_TRACKING_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        realTimeNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(realTimeNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if (!serviceKilled) {
            realTimeNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, realTimeNotificationBuilder.build())
        }
    }

    //doesn't recognize easy permission library
    @SuppressLint("MissingPermission")
    private fun updateTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermission(this)) {
                val request = LocationRequest().apply {
                    interval = HIGHEST_UPDATE_INTERVAL
                    fastestInterval = LOWEST_UPDATE_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(results: LocationResult?) {
            super.onLocationResult(results)
            if (isTracking.value!!) {
                results?.locations?.let { locations ->
                    for (location in locations) {
                        addCoordinatePoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addEmptyLine() = coordinatePoints.value?.apply {
        add(mutableListOf())
        coordinatePoints.postValue(this)
    } ?: coordinatePoints.postValue(mutableListOf(mutableListOf()))


    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                //A system service of android framework that we need to create notification.
                as NotificationManager
        //if Oreo or newer build notification.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        //updating notification without AO boiler plate code by using injection for context.
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        trailTimeInSeconds.observe(this, Observer {
            if (!serviceKilled) {
                val notification = realTimeNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            //each second a notification is sent, no need to over do it.
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

    }

    private fun startLocationForegroundService() {
        addEmptyLine()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                //A system service of android framework that we need to create notification.
                as NotificationManager
        //if Oreo or newer build notification.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        //updating notification without AO boiler plate code by using injection for context.
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        trailTimeInSeconds.observe(this, Observer {
            if (!serviceKilled) {
                val notification = realTimeNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }



}