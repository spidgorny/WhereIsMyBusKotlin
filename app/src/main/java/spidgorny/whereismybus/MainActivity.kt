package spidgorny.whereismybus

import android.annotation.SuppressLint
import android.content.Intent
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
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import io.nlopez.smartlocation.SmartLocation
import spidgorny.whereismybus.databinding.ActivityMainBinding
import spidgorny.whereismybus.databinding.ContentMainBinding
import spidgorny.whereismybus.event.ApiKeyEvent
import spidgorny.whereismybus.event.LocationSharingDisabled
import spidgorny.whereismybus.location.BusLocationService
import spidgorny.whereismybus.location.PermissionsLocation
import java.util.*


@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private val klass = "MainActivity"

//    private var location: SimpleLocation? = null

    private var sendingLocationActive = false

//    private var locationPushService: LocationPushService? = null

    private lateinit var bus: Bus

    private lateinit var bindingMain: ActivityMainBinding
    private lateinit var binding: ContentMainBinding

    private lateinit var fab: ExtendedFloatingActionButton

    private var defaultFABColor: ColorStateList? = ColorStateList.valueOf(0)

    private var jobID: Int? = null

    private var apiKey: ApiKeyEvent? = null

    lateinit var mState: Bundle;

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.addLogAdapter(AndroidLogAdapter())
        bindingMain = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingMain.root)

        binding = bindingMain.include
        this.bus = Globals.instance.getBus()
        this.bus.register(this);

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

//		this.locationPushService = LocationPushService(this)
//		JobManager.create(this).addJobCreator(UpdateLocationJobCreator())

        this.onActivityCreated()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        if (this.apiKey != null) {
            apiKey?.let {
                // Save UI state changes to the savedInstanceState.
                // This bundle will be passed to onCreate if the process is
                // killed and restarted.
                savedInstanceState.putString("apiKey", it.apiId)
                savedInstanceState.putString("apiName", it.apiName)
                savedInstanceState.putString("apiSecrete", it.apiSecret)
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mState = savedInstanceState
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        val apiId = savedInstanceState.getString("apiId")
        Log.d(this.klass, "stored apiId: $apiId")
    }

    /**
     * Does not override anything
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onActivityCreated() {
        this.initUI()
        this.initFAB()
//        this.testBus()
        restoreApiKey()
        this.updateUI()
    }

    private fun initUI() {
        val myToolbar = bindingMain.toolbar
        setSupportActionBar(myToolbar)

        // ec124bcd7a25bc02
        val deviceID = this.getDeviceID()
        deviceID?.let {
            Log.d(this.klass, "getDeviceID=$it")
            binding.deviceID.text = deviceID
            binding.deviceID.invalidate()
            binding.deviceID.requestLayout()
        }

        this.fab = bindingMain.fab
//        Log.d(this.klass, this.fab.toString())
        this.fab.isEnabled = false

//		city.text = if (BuildConfig.DEBUG) "Debug" else "Release"
//		city.text = BuildConfig.BUILD_TYPE
//		city.text = BuildConfig.IS_DEBUG_MODE.toString()
        binding.debugEnabled.text = isDebuggerConnected().toString()

//        binding.root.invalidate()
    }

    private fun testBus() {
        Log.d(this.klass, "emit 42");

        val testLocation = Location("");
        testLocation.latitude = 42.0
        testLocation.longitude = 42.0
        this.bus.post(testLocation)
    }

    @Subscribe
    fun locationUpdateFromService(event: Location) {
        Log.d(this.klass, "received $event")
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
        this.apiKey = event

        this.apiKey?.let {
            Log.d(this.klass, it.toJson())
            val sharedPreferences = getPreferences(AppCompatActivity.MODE_PRIVATE)
            it.save(sharedPreferences)
        }
        updateUI();
    }

    @Subscribe
    fun onLocationSharingDisabled(event: LocationSharingDisabled) {
        Log.d(this.klass, "onLocationSharingDisabled")
        this.disableSendingData();
    }

    private fun updateUI() {
        runOnUiThread {
            if (this.apiKey !== null) {
                this.apiKey?.let {
                    binding.deviceID.text = it.apiId
                    binding.step1Fragment.visibility = GONE
                    this.fab.isEnabled = true
                }
            } else {
                binding.deviceID.text = ""
                binding.step1Fragment.visibility = VISIBLE
                this.fab.isEnabled = false
            }
        }
    }

    private fun restoreApiKey() {
        val sharedPreferences = getPreferences(MODE_PRIVATE)
        this.apiKey = ApiKeyEvent.fromSharedPreferences(sharedPreferences)
        this.updateUI();
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
            if (this.sendingLocationActive) {
                this.disableSendingData()
            } else {
                val locationPermission = PermissionsLocation(this) {
                    this.enableSendingData()
                }
                if (!locationPermission.checkPermissions()) {
                    this.fab.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
                    locationPermission.initPermissions()
                } else {
                    this.enableSendingData()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun enableSendingData() {
        if (this.initLocation()) {
            this.sendingLocationActive = true
            this.fab.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
            this.fab.icon =
                (ContextCompat.getDrawable(this, R.drawable.baseline_block_24))
            this.fab.text = "Disable GPS"

            val startIntent = Intent(this@MainActivity, BusLocationService::class.java)
            startIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION
            this.apiKey?.let {
                startIntent.putExtra("apiSecret", it.apiSecret);
            }
            startForegroundService(startIntent)
//			Logger.i("startService", startIntent)
        } else {
            this.snack("Location is not working")
        }
    }

    private fun disableSendingData() {
        this.sendingLocationActive = false
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

        this.fab.icon =
            (ContextCompat.getDrawable(this, R.drawable.start_gps))
        this.fab.text = "Enable GPS"
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
//                bus.post(ApiKeyEvent("123", "test", "0000"))
//                testBus();
                this.apiKey?.let {
                    val sharedPreferences = getPreferences(MODE_PRIVATE)
                    it.reset(sharedPreferences)
                    this.apiKey = null;
                    this.updateUI();
                }
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

}
