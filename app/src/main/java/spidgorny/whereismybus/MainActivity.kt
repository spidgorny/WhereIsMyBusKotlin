package spidgorny.whereismybus

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
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationParams
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {


    private val klass = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

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

        SmartLocation.with(this).location().config(LocationParams.NAVIGATION)

        fab.setOnClickListener { view ->
            Log.d(this.klass, "FAB click")
            SmartLocation
                    .with(view.context)
                    .location()
                    .oneFix()
                    .start(OnLocationUpdatedListener() {
                        val location = it.latitude.toString() + "," + it.longitude.toString() + " speed: " + it.speed.toString()
                        Log.d(this.klass, location)
                        Snackbar.make(view, location.toString(), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show()

                        val response = this.pushLocation()
                        Snackbar.make(view, response.toString(), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show()
                    })
        }
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

}
