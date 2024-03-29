package spidgorny.whereismybus.location

//import android.support.v4.content.ContextCompat.startActivity
//import io.fabric.sdk.android.services.settings.IconRequest.build
//import android.support.v4.app.ServiceCompat.stopForeground
//import com.orhanobut.logger.AndroidLogAdapter
import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.orhanobut.logger.Logger
import com.squareup.otto.Bus
import spidgorny.whereismybus.Constants
import spidgorny.whereismybus.MainActivity
import spidgorny.whereismybus.R
import spidgorny.whereismybus.event.LocationSharingDisabled

class BusLocationService : Service() {

    private val klass: String = "BusLocationService"
    private lateinit var locationPushService: LocationPushService

    private val NOTIFICATION_CHANNEL_ID = "my_channel_id_01"
    private var bus: Bus? = null
    private var apiSecret: String? = ""

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Logger.i("onStartCommand")
        if (intent.action == Constants.ACTION.STARTFOREGROUND_ACTION) {
            Logger.i("Received Start Foreground Intent ")
            this.apiSecret = intent.getStringExtra("apiSecret")
            registerChannel();
            showNotification()
            Toast.makeText(this, "Location Sharing Enabled", Toast.LENGTH_SHORT).show()
            startPermissionActivity()
        } else if (intent.action == Constants.ACTION.STOPFOREGROUND_ACTION) {
            Logger.i("Received Stop Foreground Intent ")
            locationPushService.stop();
            stopForeground(true)
            stopSelf()
        }
        return START_STICKY
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(Build.VERSION_CODES.N)
    private fun registerChannel() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelName = "Where Is My Bus Channel"
        val importance = NotificationManager.IMPORTANCE_LOW
        val notificationChannel =
            NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getService(
            this, 0,
            notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // And now, building and attaching the Close button.
        val buttonCloseIntent = Intent(this, BusLocationService::class.java)
        buttonCloseIntent.action = Constants.ACTION.STOPFOREGROUND_ACTION
        val buttonClosePendingIntent =
            PendingIntent.getService(this, 0, buttonCloseIntent, PendingIntent.FLAG_IMMUTABLE)

        val icon = BitmapFactory.decodeResource(
            resources,
            R.drawable.common_full_open_on_phone
        )

        val notification = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Where is my Bus is active.")
            .setTicker("Sending location to the server...")
            .setContentText("Your location is sent to the server")
            .setSmallIcon(R.drawable.common_full_open_on_phone)
            .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "STOP",
                buttonClosePendingIntent
            )
            .setPriority(Notification.PRIORITY_DEFAULT)
            .build()

        startForeground(
            Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
            notification
        )
    }

    override fun onDestroy() {
        this.bus?.post(LocationSharingDisabled())
        super.onDestroy()
        Logger.i("onDestroy")
        Toast.makeText(this, "Location will not be sent", Toast.LENGTH_SHORT).show()
    }

    private fun startPermissionActivity() {
        this.apiSecret?.let {
            this.locationPushService.run(it)
            val delayMillis = 10000
            if (delayMillis > 0) {
                android.os.Handler().postDelayed(
                    {
                        Log.i(this.klass, "This is run $delayMillis milliseconds later")
                        this@BusLocationService.startPermissionActivity()
                    },
                    10000
                )
            }
        }

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
            message?.let {
                Logger.i(message)
            }
        }

    }

    companion object {
        private val PERM_REQUEST_LOCATION = 1
        val RESULT_OK = -1
        val KEY_MESSAGE = "KEY_MESSAGE"
        val KEY_RECEIVER = "KEY_RECEIVER"
    }
}
