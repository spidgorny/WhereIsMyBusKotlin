package spidgorny.whereismybus

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.budiyev.android.codescanner.*
import com.google.zxing.BarcodeFormat

class ScannerActivity : AppCompatActivity() {
    private var codeScanner: CodeScanner? = null
    val klass = "ScannerActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scanner_view)
        val camera = PermissionsCamera(this) {
            startScanning()
        }
        camera.setupPermissions()
    }


    private fun startScanning() {
        val scannerView = findViewById<CodeScannerView>(R.id.code_scanner_view)
        codeScanner = CodeScanner(this, scannerView)
        codeScanner?.let {

            // Parameters (default values)
            it.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
            it.formats = listOf(BarcodeFormat.QR_CODE) // list of type BarcodeFormat,
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