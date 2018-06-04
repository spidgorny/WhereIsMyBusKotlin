package spidgorny.whereismybus

import android.Manifest
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import android.os.StrictMode
import android.util.Log
//import im.delight.android.location.SimpleLocation
//import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import kotlinx.android.synthetic.main.content_main.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Debug.isDebuggerConnected
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
//import com.evernote.android.job.JobManager
//import okhttp3.*
import com.crashlytics.android.Crashlytics
import com.squareup.leakcanary.LeakCanary
import io.fabric.sdk.android.Fabric

class MainActivity : AppCompatActivity() {

    private val klass = "MainActivity"

//    private var location: SimpleLocation? = null

    private val MY_PERMISSIONS_REQUEST_LOCATION = 1

	private var enabled = false

	private var locationPushService: LocationPushService? = null

	override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

		Fabric.with(this, Crashlytics())

		setContentView(R.layout.activity_main)
		setSupportActionBar(toolbar)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

		this.onActivityCreated()

		this.locationPushService = LocationPushService(this)
//		JobManager.create(this).addJobCreator(UpdateLocationJobCreator())

//		city.text = if (BuildConfig.DEBUG) "Debug" else "Release"
//		city.text = BuildConfig.BUILD_TYPE
//		city.text = BuildConfig.IS_DEBUG_MODE.toString()
		city.text = isDebuggerConnected().toString()
	}

	/**
	 * Does not override anything
	 */
	fun onActivityCreated() {
		this.initUI()
		this.initFAB()
	}

	private fun checkPermissions(): Boolean {
		if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
				PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
				PackageManager.PERMISSION_GRANTED) {
			Log.d(this.klass, "Permissions OK")
			return true
		}
		return false
	}

	private fun initPermissions() {
		Log.d(this.klass, "Permissions Request")
		ActivityCompat.requestPermissions(this, arrayOf(
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.ACCESS_COARSE_LOCATION),
				MY_PERMISSIONS_REQUEST_LOCATION)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    this.enableSendingData()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

        // Add other 'when' lines to check for other
        // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun initUI() {
        // ec124bcd7a25bc02
        Log.d(this.klass, this.getDeviceID())
        this.deviceID.text = this.getDeviceID()
    }

    fun getDeviceID(): String? {
        return Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID);
    }

	private var defaultFABColor: ColorStateList? = ColorStateList.valueOf(0)

	private var jobID: Int? = null

	private fun initFAB() {
		Log.d(this.klass, "FAB Color: " + fab.backgroundTintList)
		this.defaultFABColor = fab.backgroundTintList // -49023
		fab.setOnClickListener { view: View ->
			Log.d(this.klass, "FAB click")

//			Crashlytics.sharedInstance().crash()
//			throw RuntimeException("Shit happens")

//            val latitude = location!!.latitude
//            val longitude = location!!.longitude
//            val speed = location!!.speed
			if (this.enabled) {
				this.enabled = false
				this.jobID?.let {
//					JobManager.instance().cancel(it)
				}
				fab.backgroundTintList = if (this.defaultFABColor != null)
					this.defaultFABColor
					else ColorStateList.valueOf(Color.RED)
			} else {
				if (!this.checkPermissions()) {
					fab.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
					this.initPermissions()
				} else {
					this.enableSendingData()
				}
			}
		}
	}

	protected fun enableSendingData() {
		if (this.initLocation()) {
			this.enabled = true
			fab.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
//			this.updateLocation()
			this.locationPushService?.run()
//			this.jobID = UpdateLocationJob.scheduleJob()
			android.os.Handler().postDelayed(
					{
//						Log.i(this.klass, "This'll run 3000 milliseconds later")
						this.enableSendingData()	// periodic
					},
					60000)
		}
	}

	private fun initLocation(): Boolean {
//        val context = getActivity(this).findViewById(android.R.id.content)
//		val context = this.layout1

		val lsEnabled = SmartLocation.with(this.applicationContext).location().state().locationServicesEnabled()
		Log.d(this.klass, "Location Services: $lsEnabled")
		if (!lsEnabled) {
//			Snackbar.make(context, "LocationService not enabled", Snackbar.LENGTH_LONG)
//					.setAction("Action", null).show()
		} else {
//			Snackbar.make(context, "LocationService OK", Snackbar.LENGTH_LONG)
//					.setAction("Action", null).show()
		}

		val gpsEnabled = SmartLocation.with(this.applicationContext).location().state().isGpsAvailable
		Log.d(this.klass, "GPS: $lsEnabled")
		if (!gpsEnabled) {
//			Snackbar.make(context, "GPS not enabled", Snackbar.LENGTH_LONG)
//					.setAction("Action", null).show()
		} else {
//			Snackbar.make(context, "GPS OK", Snackbar.LENGTH_LONG)
//					.setAction("Action", null).show()
		}
		return lsEnabled && gpsEnabled
	}

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
				finish()
				return true
			}
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val context = this.applicationContext
        SmartLocation.with(context).location().stop()
    }

    override fun onResume() {
        super.onResume()

        // make the device update its location
//        if (this.location != null) {
//            this.location!!.beginUpdates()
//        }

        // ...
    }

    override fun onPause() {
        // stop location updates (saves battery)
//        if (this.location != null) {
//            this.location!!.endUpdates()
//        }

        // ...

        super.onPause()
    }

	override fun onBackPressed() {
		moveTaskToBack(true)
	}

}
