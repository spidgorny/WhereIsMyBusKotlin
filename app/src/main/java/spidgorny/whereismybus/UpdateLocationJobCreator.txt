package spidgorny.whereismybus

import android.support.annotation.NonNull
import android.support.annotation.Nullable
import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator


class UpdateLocationJobCreator : JobCreator {

	@Nullable
	override fun create(tag: String): Job? {
		when (tag) {
			UpdateLocationJob.TAG -> return UpdateLocationJob(LocationPushService(MainActivity()))
			else -> return null
		}
	}
}
