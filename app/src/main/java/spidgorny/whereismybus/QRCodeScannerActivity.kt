package spidgorny.whereismybus


import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.g00fy2.quickie.ScanQRCode

class QRCodeScannerActivity : AppCompatActivity() {

    private val scanQrCodeLauncher = registerForActivityResult(ScanQRCode()) { result ->
        // handle QRResult
        Log.d("QRCodeScannerActivity", result.toString())
        runOnUiThread {
            Toast.makeText(this, "Scan result: $result", Toast.LENGTH_LONG).show()
        }
    }

    val klass = "QRCodeScannerActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.scanner_view)
        val camera = PermissionsCamera(this) {
            startScanning()
        }
        camera.setupPermissions()
    }

    private fun startScanning() {
        scanQrCodeLauncher.launch(null)
    }

}