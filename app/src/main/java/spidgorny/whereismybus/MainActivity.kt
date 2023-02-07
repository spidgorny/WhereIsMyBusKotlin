package spidgorny.whereismybus

//import im.delight.android.location.SimpleLocation
//import io.nlopez.smartlocation.OnLocationUpdatedListener
//import com.evernote.android.job.JobManager
//import okhttp3.*
//import android.support.design.widget.Snackbar
//import android.support.v4.app.ActivityCompat
//import android.support.v4.content.ContextCompat
//import android.support.v7.app.AppCompatActivity
//import com.crashlytics.android.Crashlytics
//import io.fabric.sdk.android.Fabric
//import kotlinx.android.synthetic.main.activity_main.*
//import kotlinx.android.synthetic.main.content_main.*

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Debug.isDebuggerConnected
import android.os.StrictMode
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import io.nlopez.smartlocation.SmartLocation
import spidgorny.whereismybus.databinding.ActivityMainBinding
import spidgorny.whereismybus.databinding.ContentMainBinding
import java.util.*


class MainActivity : AppCompatActivity() {

    private val klass = "MainActivity"

//    private var location: SimpleLocation? = null

    private val MY_PERMISSIONS_REQUEST_LOCATION = 1

    private var enabled = false

//    private var locationPushService: LocationPushService? = null

    private var bus: Bus? = null

    private lateinit var bindingMain: ActivityMainBinding
    private lateinit var binding: ContentMainBinding

    private lateinit var fab: FloatingActionButton

    private var defaultFABColor: ColorStateList? = ColorStateList.valueOf(0)

    private var jobID: Int? = null

    private lateinit var apiKey: ApiKeyEvent

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.addLogAdapter(AndroidLogAdapter())
        bindingMain = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingMain.root)

        binding = bindingMain.include
        this.bus = Globals.instance.getBus()

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

//		this.locationPushService = LocationPushService(this)
//		JobManager.create(this).addJobCreator(UpdateLocationJobCreator())

        this.onActivityCreated()
    }

    /**
     * Does not override anything
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onActivityCreated() {
        this.initUI()
        this.initFAB()
        this.testBus();
    }

    private fun initUI() {
        val myToolbar = bindingMain.toolbar
        setSupportActionBar(myToolbar)

        // ec124bcd7a25bc02
        val deviceID = this.getDeviceID()
        deviceID?.let {
            Log.d(this.klass, "getDeviceID=$it")
            binding.deviceID.text = deviceID
            binding.deviceID.invalidate();
            binding.deviceID.requestLayout();
        }

        this.fab = bindingMain.fab
        Log.d(this.klass, this.fab.toString())

//		city.text = if (BuildConfig.DEBUG) "Debug" else "Release"
//		city.text = BuildConfig.BUILD_TYPE
//		city.text = BuildConfig.IS_DEBUG_MODE.toString()
        binding.debugEnabled.text = isDebuggerConnected().toString()

        binding.root.invalidate()
    }

    private fun testBus() {
        this.bus!!.register(this);
        Log.d(this.klass, "emit 42");

        val testLocation = Location("");
        testLocation.latitude = 42.0
        testLocation.longitude = 42.0
        this.bus!!.post(testLocation)
    }

    @Subscribe
    fun locationUpdateFromService(event: Location) {
        Log.d(this.klass, "received " + event.toString())
        Logger.i("received", event)
        runOnUiThread {
            binding.updated.text = Calendar.getInstance().getTime().toString();
            binding.tvLocation.text = "${event.latitude}, ${event.longitude}";
            binding.tvLocation.invalidate();
            binding.tvLocation.requestLayout();
            Log.d(this.klass, "tvLocation: ${this.binding.tvLocation.text}")
        }
    }

    @Subscribe
    fun apiKeyScanned(event: ApiKeyEvent) {
        Log.d(this.klass, "apiKeyScanned: $event");
        this.apiKey = event;
    }

    private fun checkPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(this.klass, "Permissions OK")
            return true
        }
        return false
    }

    private fun initPermissions() {
        Log.d(this.klass, "Permissions Request")
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

    fun getDeviceID(): String? {
        return Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        );
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initFAB() {
//		Log.d(this.klass, "FAB Color: " + fab.backgroundTintList)
        this.defaultFABColor = this.fab.backgroundTintList // -49023
        this.fab.setOnClickListener { _: View ->
            Log.d(this.klass, "FAB click")

//            val latitude = location!!.latitude
//            val longitude = location!!.longitude
//            val speed = location!!.speed
            if (this.enabled) {
                this.disableSendingData()
            } else {
                if (!this.checkPermissions()) {
                    this.fab.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
                    this.initPermissions()
                } else {
                    this.enableSendingData()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun enableSendingData() {
        if (this.initLocation()) {
            this.enabled = true
            this.fab.backgroundTintList = ColorStateList.valueOf(Color.GREEN)

            val startIntent = Intent(this@MainActivity, BusLocationService::class.java)
            startIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION
            startForegroundService(startIntent)
//			Logger.i("startService", startIntent)
        } else {
            this.snack("Location is not working")
        }
    }

    private fun disableSendingData() {
        this.enabled = false
        this.jobID?.let {
            //	JobManager.instance().cancel(it)
        }
        bindingMain.fab.backgroundTintList = if (this.defaultFABColor != null)
            this.defaultFABColor
        else ColorStateList.valueOf(Color.RED)

        val startIntent = Intent(this@MainActivity, BusLocationService::class.java)
        startIntent.action = Constants.ACTION.STOPFOREGROUND_ACTION
        startService(startIntent)
//		Logger.i("startService", startIntent)
    }

    private fun initLocation(): Boolean {
//        val context = getActivity(this).findViewById(android.R.id.content)
//		val context = this.layout1

        val lsEnabled =
            SmartLocation.with(this.applicationContext).location().state().locationServicesEnabled()
        Log.d(this.klass, "Location Services: $lsEnabled")
        if (!lsEnabled) {
//			Snackbar.make(context, "LocationService not enabled", Snackbar.LENGTH_LONG)
//					.setAction("Action", null).show()
        } else {
//			Snackbar.make(context, "LocationService OK", Snackbar.LENGTH_LONG)
//					.setAction("Action", null).show()
        }

        val gpsEnabled =
            SmartLocation.with(this.applicationContext).location().state().isGpsAvailable
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
                return true
            }
            R.id.action_crash -> {
                throw RuntimeException("Shit happens")
            }
            R.id.action_exit -> {
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

    fun snack(text: String) {
        Snackbar.make(bindingMain.layout1, text, Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        if (this::apiKey.isInitialized) {
            apiKey?.let {
                // Save UI state changes to the savedInstanceState.
                // This bundle will be passed to onCreate if the process is
                // killed and restarted.
                savedInstanceState.putString("apiKey", apiKey!!.apiId)
                savedInstanceState.putString("apiName", apiKey!!.apiName)
                savedInstanceState.putString("apiSecrete", apiKey!!.apiSecret)
            }
        }
    }
}
