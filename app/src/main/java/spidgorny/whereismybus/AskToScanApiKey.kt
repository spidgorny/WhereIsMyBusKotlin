package spidgorny.whereismybus

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class AskToScanApiKey : Fragment() {

    companion object {
        fun newInstance() = AskToScanApiKey()
    }

    private lateinit var viewModel: AskToScanApiKeyViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ask_to_scan_api_key, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AskToScanApiKeyViewModel::class.java)
        // TODO: Use the ViewModel
    }

}