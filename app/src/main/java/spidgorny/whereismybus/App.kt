package spidgorny.whereismybus

import android.app.Application
import android.util.Log
import com.evernote.android.job.JobManager


class App : Application() {

	private val klass = "App"

	override fun onCreate() {
		Log.d(this.klass, "App.onCreate")
		super.onCreate()
		JobManager.create(this).addJobCreator(UpdateLocationJobCreator())
	}
}
