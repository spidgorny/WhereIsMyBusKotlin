package spidgorny.whereismybus

import android.Manifest
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import android.os.StrictMode
import android.util.Log
import im.delight.android.location.SimpleLocation
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationParams
import kotlinx.android.synthetic.main.content_main.*
import android.Manifest.permission
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat


class MainActivity : AppCompatActivity() {

    private val klass = "MainActivity"

    private var location: SimpleLocation? = null

    private val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            Log.d(this.klass, "Permissions OK")
            this.initLocation()
        } else {
            Log.d(this.klass, "Permissions Request")
            ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS)
        }

        val lsEnabled = SmartLocation.with(this.applicationContext).location().state().locationServicesEnabled()
        if (!lsEnabled) {
            Snackbar.make(top_view, "LocationService not enabled", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        } else {
            Snackbar.make(top_view, "LocationService OK", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val gpsEnabled = SmartLocation.with(this.applicationContext).location().state().isGpsAvailable
        if (!gpsEnabled) {
            Snackbar.make(top_view, "GPS not enabled", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        } else {
            Snackbar.make(top_view, "GPS OK", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        fab.setOnClickListener { view ->
            Log.d(this.klass, "FAB click")
            val latitude = location!!.latitude
            val longitude = location!!.longitude
            val speed = location!!.speed
            val location = latitude.toString() + "," + longitude.toString() + " speed: " + speed.toString()
            Log.d(this.klass, location)
            Snackbar.make(view, location, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()

            val response = this.pushLocation()
            Snackbar.make(view, response.toString(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    this.initLocation()
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

    private fun initLocation() {
        Log.d(this.klass, "location is set")
        this.location = SimpleLocation(this, true, false, 5 * 1000, true)
        // if we can't access the location yet
        if (!this.location!!.hasLocationEnabled()) {
            // ask the user to enable location access
            SimpleLocation.openSettings(this)
        }

        location!!.setListener({
            fun onPositionChanged() {
                val latitude = location!!.latitude
                val longitude = location!!.longitude
                val speed = location!!.speed
                val location = latitude.toString() + "," + longitude.toString() + " speed: " + speed.toString()
                Log.d(this.klass + " onPositionChanged", location)
            }
        })
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
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun pushLocation(): String? {
        val client = OkHttpClient()

        val url = "http://192.168.1.6/robots.txt"
        val request = Request.Builder()
                .url(url)
                .build()

        try {
            val response = client.newCall(request).execute()
            return response.body()?.string()
        } catch (e: IOException) {
            return e.toString()
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
        if (this.location != null) {
            this.location!!.beginUpdates()
        }

        // ...
    }

    override fun onPause() {
        // stop location updates (saves battery)
        if (this.location != null) {
            this.location!!.endUpdates()
        }

        // ...

        super.onPause()
    }
}
