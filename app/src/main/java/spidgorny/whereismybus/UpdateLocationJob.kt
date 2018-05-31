package spidgorny.whereismybus

import com.evernote.android.job.JobRequest
import com.evernote.android.job.Job.Params
import android.support.annotation.NonNull
import android.util.Log
import com.evernote.android.job.Job


class UpdateLocationJob(lps: LocationPushService) : Job() {

	private val klass = "UpdateLocationJob"

	private val locationPushService: LocationPushService = lps

	override fun onRunJob(params: Params): Job.Result {
		// run your job here
		Log.d(this.klass, "onRunJob")
		this.locationPushService.run()
		return Job.Result.SUCCESS
	}

	fun runOnce() {
		this.locationPushService.run()
	}
	companion object {

		const val TAG = "UpdateLocationJob"

		fun scheduleJob(): Int {
			return JobRequest.Builder(UpdateLocationJob.TAG)
					.setExecutionWindow(30_000L, 40_000L)
					.build()
					.schedule()
		}

	}

}
