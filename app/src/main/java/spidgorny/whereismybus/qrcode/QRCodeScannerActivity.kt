package spidgorny.whereismybus.qrcode


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode

class QRCodeScannerActivity : AppCompatActivity() {

    val klass = "QRCodeScannerActivity"

    private val scanQrCodeLauncher = registerForActivityResult(ScanQRCode()) { result: QRResult ->
        // handle QRResult
        Log.d("QRCodeScannerActivity", result.toString())
        runOnUiThread {
            if (result is QRResult.QRSuccess) {
                val success: QRResult.QRSuccess = result;
                Toast.makeText(this, "Scan result: $result", Toast.LENGTH_LONG).show()
                val data = Intent()
                data.putExtra("text", success.content.rawValue)
                setResult(RESULT_OK, data)
                finish()
            }
        }
    }

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