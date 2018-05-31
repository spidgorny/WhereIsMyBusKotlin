package spidgorny.whereismybus

import android.content.Context
import android.support.design.widget.Snackbar
import android.util.Log
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.*
import java.io.IOException

class LocationPushService(protected val activity: MainActivity) {

	protected val klass = "LocationPushService"

	fun run() {
		Log.d(this.klass, "locationPushService.run")
		this.updateLocation()
	}

	protected fun updateLocation() {
		Snackbar.make(this.activity.layout1, "Checking GPS...", Snackbar.LENGTH_LONG)
				.setAction("Action", null).show()
		SmartLocation.with(this.activity.applicationContext).location()
				.oneFix()
				.start(OnLocationUpdatedListener() {
					val latitude = it.latitude
					val longitude = it.longitude
					val speed = it.speed
					val bearing = it.bearing

					val sLocation = latitude.toString() + ", " + longitude.toString()+
							" speed: " + speed.toString() +
							" bearing: " + bearing.toString()
					Log.d(this.klass, sLocation)

					this.activity.tvLocation.text = sLocation

//					Snackbar.make(this.activity.layout1, sLocation, Snackbar.LENGTH_LONG)
//							.setAction("Action", null).show()

					Log.d(this.klass, "Pushing...")
					this.pushLocation(latitude, longitude, speed, bearing)
				})
	}

	fun pushLocation(lat: Double, lon: Double, speed: Float, bearing: Float) {
		val client = OkHttpClient()

		val url = "https://where-is-my-bus.now.sh/ping"
		val builder = HttpUrl.parse(url)?.newBuilder()
		builder?.addQueryParameter("deviceid", this.activity.getDeviceID())
		builder?.addQueryParameter("lat", lat.toString())
		builder?.addQueryParameter("lon", lon.toString())
		builder?.addQueryParameter("speed", speed.toString())
		builder?.addQueryParameter("bearing", bearing.toString())

		builder?.let {
			Log.d(this.klass, builder.build().toString())
			val request = Request.Builder()
					.url(builder.build().toString())
					.header("Authorization", "Bearer SJrIEduYXwPME7hm8y5_gPTBI7-luF-rc86qfCsbXek")
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

					this@LocationPushService.activity.runOnUiThread {
						Snackbar.make(this@LocationPushService.activity.layout1, html ?: "", Snackbar.LENGTH_LONG)
								.setAction("Action", null).show()
					}
				}
			})
		}
	}

}