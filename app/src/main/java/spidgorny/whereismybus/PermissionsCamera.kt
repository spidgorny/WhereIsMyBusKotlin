package spidgorny.whereismybus

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionsCamera(val context: Activity, val startScanning: () -> Unit) :
    AppCompatActivity() {
    val klass = "PermissionsCamera"
    private val RECORD_REQUEST_CODE = 101

    fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this.context,
            Manifest.permission.CAMERA
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(this.klass, "Permission to record denied")
            makeRequest()
        } else {
            this.startScanning()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this.context,
            arrayOf(Manifest.permission.CAMERA),
            RECORD_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RECORD_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i(this.klass, "Permission has been denied by user")
                } else {
                    Log.i(this.klass, "Permission has been granted by user")
                    startScanning()
                }
            }
        }
    }
}