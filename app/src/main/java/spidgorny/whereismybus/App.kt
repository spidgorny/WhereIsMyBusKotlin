package spidgorny.whereismybus

import android.app.Application
import android.util.Log
import com.squareup.leakcanary.LeakCanary
//import com.evernote.android.job.JobManager

class App : Application() {

	private val klass = "App"

	override fun onCreate() {
		Log.d(this.klass, "App.onCreate")
		super.onCreate()
//		JobManager.create(this).addJobCreator(UpdateLocationJobCreator())

		if (LeakCanary.isInAnalyzerProcess(this)) {
			// This process is dedicated to LeakCanary for heap analysis.
			// You should not init your app in this process.
			return;
		}
		LeakCanary.install(this)

	}
}
