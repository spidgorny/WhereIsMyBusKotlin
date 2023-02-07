package spidgorny.whereismybus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider


class AskToScanApiKey : Fragment() {

    val klass = "AskToScanApiKey"

    companion object {
        fun newInstance() = AskToScanApiKey()
    }

    private lateinit var viewModel: AskToScanApiKeyViewModel

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
            onClickScanQr(this.context!!)
        }

        return bind;
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AskToScanApiKeyViewModel::class.java)
        // TODO: Use the ViewModel
    }

    fun onClickScanQr(context: Context) {
        Log.d(this.klass, "onClickScanQr")
        val myIntent = Intent(context, ScannerActivity::class.java)
        myIntent.putExtra("key", 12) //Optional parameters
        this@AskToScanApiKey.startActivity(myIntent)
    }

}