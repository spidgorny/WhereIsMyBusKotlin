package spidgorny.whereismybus

import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.content.Intent
import android.widget.Toast
import android.graphics.Bitmap
import io.fabric.sdk.android.services.settings.IconRequest.build
import android.graphics.BitmapFactory
import android.app.PendingIntent
import android.app.Service
import android.support.v4.app.ServiceCompat.stopForeground
import android.os.IBinder
import android.os.ResultReceiver
import android.support.v4.app.NotificationCompat
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger


class BusLocationService : Service() {

	private lateinit var locationPushService: LocationPushService

	private val NOTIFICATION_CHANNEL_ID = "my_channel_id_01"

	override fun onCreate() {
		super.onCreate()
//		Logger.addLogAdapter(AndroidLogAdapter())
		Logger.i("onCreate")

		this.locationPushService = LocationPushService(this)
	}

	override fun onBind(intent: Intent): IBinder? {
		// Used only in case if services are bound (Bound Services).
		return null
	}

	override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
		Logger.i("onStartCommand")
		if (intent.action == Constants.ACTION.STARTFOREGROUND_ACTION) {
			Logger.i("Received Start Foreground Intent ")
			showNotification()
			Toast.makeText(this, "Service Started!", Toast.LENGTH_SHORT).show()
			startPermissionActivity()
		} else if (intent.action == Constants.ACTION.STOPFOREGROUND_ACTION) {
			Logger.i("Received Stop Foreground Intent ")
			stopForeground(true)
			stopSelf()
		}
		return START_STICKY
	}

	private fun showNotification() {
		val notificationIntent = Intent(this, MainActivity::class.java)
		notificationIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION
		notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
		val pendingIntent = PendingIntent.getService(this, 0,
				notificationIntent, 0)

		// And now, building and attaching the Close button.
		val buttonCloseIntent = Intent(this, BusLocationService::class.java)
		buttonCloseIntent.action = Constants.ACTION.STOPFOREGROUND_ACTION
		val buttonClosePendingIntent = PendingIntent.getService(this, 0, buttonCloseIntent, 0)

		val icon = BitmapFactory.decodeResource(resources,
				R.drawable.common_full_open_on_phone)

		val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
				.setContentTitle("AndroidGuitar")
				.setTicker("AndroidGuitar")
				.setContentText("Ready to play!")
				.setSmallIcon(R.drawable.common_full_open_on_phone)
				.setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
				.setContentIntent(pendingIntent)
				.setOngoing(true)
				.addAction(android.R.drawable.ic_menu_close_clear_cancel, "close", buttonClosePendingIntent)
				.build()

		startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
				notification)

	}

	override fun onDestroy() {
		super.onDestroy()
		Logger.i("inDestroy")
		Toast.makeText(this, "Service Destroyed!", Toast.LENGTH_SHORT).show()
	}


	private fun startPermissionActivity() {
		this.locationPushService.run()
		android.os.Handler().postDelayed(
				{
					//							Log.i(this.klass, "This'll run 3000 milliseconds later")
					this@BusLocationService.startPermissionActivity()
				},
				60000)


//		val intent = Intent(this, MainActivity::class.java)
//		intent.putExtra(KEY_RECEIVER, MessageReceiver())
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//		startActivity(intent)
	}

	internal inner class MessageReceiver : ResultReceiver(null) {
		protected override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
			Logger.i("onReceiveResult")
			if (resultCode != RESULT_OK) {
				return
			}
			val message = resultData.getString(KEY_MESSAGE)
			Logger.i(message)
		}

	}

	companion object {
		private val PERM_REQUEST_LOCATION = 1
		val RESULT_OK = -1
		val KEY_MESSAGE = "KEY_MESSAGE"
		val KEY_RECEIVER = "KEY_RECEIVER"
	}
}
