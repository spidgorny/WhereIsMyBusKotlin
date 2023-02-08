package spidgorny.whereismybus

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*

class ScannerActivity : AppCompatActivity() {
    private var codeScanner: CodeScanner? = null
    val klass = "ScannerActivity"
    private val RECORD_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scanner_view)
        setupPermissions()
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(this.klass, "Permission to record denied")
            makeRequest()
        } else {
            startScanning()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
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

    fun startScanning() {
        val scannerView = findViewById<CodeScannerView>(R.id.code_scanner_view)
        codeScanner = CodeScanner(this, scannerView)
        codeScanner?.let {

            // Parameters (default values)
            it.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
            it.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
            // ex. listOf(BarcodeFormat.QR_CODE)
            it.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
            it.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
            it.isAutoFocusEnabled = true // Whether to enable auto focus or not
            it.isFlashEnabled = false // Whether to enable flash or not

            // Callbacks
            it.decodeCallback = DecodeCallback {
                runOnUiThread {
                    Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
                    val data = Intent()
                    data.putExtra("text", it.text)
                    setResult(RESULT_OK, data)
                    finish()
                }
            }
            it.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
                runOnUiThread {
                    Toast.makeText(
                        this, "Camera initialization error: ${it.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            val cs = it;
            scannerView.setOnClickListener {
                cs.startPreview()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner?.let { it.startPreview() }
    }

    override fun onPause() {
        codeScanner?.let { it.releaseResources() }
        super.onPause()
    }
}