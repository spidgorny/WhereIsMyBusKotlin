package spidgorny.whereismybus

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.squareup.otto.Bus
import org.json.JSONException
import org.json.JSONObject


class AskToScanApiKey : Fragment() {

    val klass = "AskToScanApiKey"
    private var bus: Bus? = null

    companion object {
        fun newInstance() = AskToScanApiKey()
    }

    private lateinit var viewModel: AskToScanApiKeyViewModel

    private val intentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val qrCodeJson = result.data?.getStringExtra("text")
            qrCodeJson?.let {
                onQrCodeScanned(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bind = inflater.inflate(R.layout.fragment_ask_to_scan_api_key, container, false)
//        bind.root.btnScanQrCode.setOnClickListener {
//            onClickScanQr()
//        }
        val button: Button = bind.findViewById(R.id.btnScanQrCode) as Button
        button.setOnClickListener {
            onClickScanQr(this.requireContext())
        }

        this.bus = Globals.instance.getBus()
        return bind;
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AskToScanApiKeyViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun onClickScanQr(context: Context) {
        Log.d(this.klass, "onClickScanQr")
//        val myIntent = Intent(context, ScannerActivity::class.java)
//        myIntent.putExtra("key", 12) //Optional parameters
//        this@AskToScanApiKey.startActivityForResult(myIntent, REQUEST_QR_CODE)

        intentLauncher.launch(Intent(requireContext(), ScannerActivity::class.java))
//        intentLauncher.launch(Intent(requireContext(), QRCodeScannerActivity::class.java))
    }

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Handle the returned Uri
        onQrCodeScanned(uri.toString())
    }

    fun onQrCodeScanned(text: String) {
        Log.d(this.klass, text);
        try {
            val jObject = JSONObject(text);
            val apiId = jObject.getString("apiId");
            val apiName = jObject.getString("apiName");
            val apiSecret = jObject.getString("apiSecret");
            Log.d(this.klass, "apiId: $apiId");
            Log.d(this.klass, "apiName: $apiName");
            Log.d(this.klass, "apiSecret: $apiSecret");

            bus!!.post(ApiKeyEvent(apiId, apiName, apiSecret))
        } catch (e: JSONException) {
            requireActivity().runOnUiThread {
                Toast.makeText(
                    requireContext(), "Invalid QR Code, must be scanned from Where-is-my.bus site",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}