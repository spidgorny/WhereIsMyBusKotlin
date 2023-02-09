package spidgorny.whereismybus

//import android.support.design.widget.Snackbar
//import kotlinx.android.synthetic.main.activity_main.*
//import kotlinx.android.synthetic.main.content_main.*

import android.content.Context
import android.content.ContextWrapper
import android.location.Location
import android.provider.Settings
import android.util.Log
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.squareup.otto.Bus
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import okhttp3.*
import java.io.IOException


class LocationPushService(base: Context) : ContextWrapper(base) {

    protected val klass = "LocationPushService"
    private var bus: Bus? = null
    private var apiSecret: String? = ""

    fun run(apiSecret: String) {
        Log.d(this.klass, "locationPushService.run")
        this.apiSecret = apiSecret
        this.bus = Globals.instance.getBus()
        Logger.addLogAdapter(AndroidLogAdapter())
        this.updateLocation()
    }

    protected fun updateLocation() {
//		Snackbar.make(this, "Checking GPS...", Snackbar.LENGTH_LONG)
//				.setAction("Action", null).show()
        SmartLocation.with(this).location()
//            .oneFix()
            .start(OnLocationUpdatedListener() {
                val latitude = it.latitude
                val longitude = it.longitude
                val speed = it.speed
                val bearing = it.bearing

                val sLocation = "$latitude, $longitude speed: $speed bearing: $bearing"
                Log.d(this.klass, sLocation)

                this.updateUIlocation(it)

//					Snackbar.make(this.activity.layout1, sLocation, Snackbar.LENGTH_LONG)
//							.setAction("Action", null).show()

                if (this.apiSecret?.isNotEmpty() == true) {
                    Log.d(this.klass, "Pushing...")
                    this.pushLocation(latitude, longitude, speed, bearing)
                } else {
                    Log.d(this.klass, "apiSecret is empty")
                }
            })
    }

    fun pushLocation(lat: Double, lon: Double, speed: Float, bearing: Float) {
        val client = OkHttpClient()

        val url = "https://where-is-my-bus-now.vercel.app/api/v1/gps"
//        val url = "http://10.0.2.2:3000/api/v1/gps"
        val builder = HttpUrl.parse(url)?.newBuilder()
//        builder?.addQueryParameter("deviceid", this.getDeviceID())
//        builder?.addQueryParameter("lat", lat.toString())
//        builder?.addQueryParameter("lon", lon.toString())
//        builder?.addQueryParameter("speed", speed.toString())
//        builder?.addQueryParameter("bearing", bearing.toString())

        builder?.let {
            Log.d(this.klass, builder.build().toString())

            val formBody: RequestBody = FormBody.Builder()
                .add("lat", lat.toString())
                .add("lon", lon.toString())
                .build()


            Logger.i("x-api-key=${this.apiSecret}")
            val request = Request.Builder()
                .url(builder.build().toString())
                .header("content-type", "application/json")
                .header("x-api-key", this.apiSecret!!)
                .method("POST", formBody)
                .build()

            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }
                    val html = response.body()?.string()
                    this@LocationPushService.updateUIsnack(html ?: "")
                }
            })
        }
    }

    fun getDeviceID(): String? {
        return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID);
    }

    fun updateUIlocation(loc: Location) {
//		this.activity.tvLocation.text = sLocation
        this.bus?.post(loc)

    }

    fun updateUIsnack(snack: String) {
//		this.context.runOnUiThread {
//			Snackbar.make(this@LocationPushService.activity.layout1, snack, Snackbar.LENGTH_LONG)
//					.setAction("Action", null).show()
//		}
    }

}