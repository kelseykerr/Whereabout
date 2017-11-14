package com.kelseykerr.whereabout

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context

/**
 * Created by kelseykerr on 11/14/17.
 */
class LocationJobScheduler(context: Context) : JobService() {

    var context: Context = context
    private val locationService: LocationService = LocationService(context)

    companion object {
        const val JOB_ID = 120
    }

    override fun onStartJob(params: JobParameters): Boolean {
        locationService.getLocation()
        scheduleJob()
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return true
    }

    fun scheduleJob() {
        val sharedPrefs = context.getSharedPreferences(Utils.SETTINGS_NAME, 0)
        val scanMinLatency = sharedPrefs.getLong("scanFrequencyMins", 15)
        val serviceComponent = ComponentName(context, LocationJobScheduler::class.java)
        val builder = JobInfo.Builder(JOB_ID, serviceComponent)
        builder.setPersisted(true)
        builder.setMinimumLatency(scanMinLatency * 60000)
        builder.setOverrideDeadline((scanMinLatency + 5) * 60000)
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(builder.build())
    }


}