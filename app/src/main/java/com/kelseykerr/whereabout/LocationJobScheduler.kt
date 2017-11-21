package com.kelseykerr.whereabout

import android.Manifest
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.util.Log

/**
 * Created by kelseykerr on 11/14/17.
 */
class LocationJobScheduler : JobService() {

    private lateinit var context: Context
    private lateinit var locationService: LocationService

    companion object {
        const val JOB_ID = 120
        const val TAG = "LocationJobScheduler"
    }

    override fun onStartJob(params: JobParameters): Boolean {
        this.locationService = LocationService(applicationContext)
        val hasCoarsePerm = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasFinePerm = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasCoarsePerm && !hasFinePerm) {
            Log.d(TAG, "No location permissions, skipping update")
        } else {
            locationService.getLocation()
        }
        scheduleJob()
        return true
    }

    override fun onStopJob(params: JobParameters) = true

    private fun scheduleJob() {
        val sharedPrefs = applicationContext.getSharedPreferences(Utils.SETTINGS_NAME, 0)
        val scanMinLatency = sharedPrefs.getLong("scanFrequencyMins", 15)
        Log.d(TAG, "Scheduling next location update with min latency of [" +
                scanMinLatency + "] mins and a max latency of [" + (scanMinLatency + 5) + "] mins.")
        val serviceComponent = ComponentName(applicationContext, LocationJobScheduler::class.java)
        val builder = JobInfo.Builder(JOB_ID, serviceComponent)
        builder.setPersisted(true)
        builder.setMinimumLatency(scanMinLatency * 60000)
        builder.setOverrideDeadline((scanMinLatency + 5) * 60000)
        val jobScheduler = applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(builder.build())
    }

    fun startUpdates(context: Context) {
        this.context = context
        this.locationService = LocationService(context)
        Log.d(TAG, "Starting location updates")
        val serviceComponent = ComponentName(context, LocationJobScheduler::class.java)
        val builder = JobInfo.Builder(JOB_ID, serviceComponent)
        builder.setPersisted(true)
        builder.setMinimumLatency(10000)
        builder.setOverrideDeadline(20000)
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(builder.build())
    }


}