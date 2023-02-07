package spidgorny.whereismybus

import android.util.Log
import androidx.lifecycle.ViewModel

class AskToScanApiKeyViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    val klass = "AskToScanApiKeyViewMode"

    fun onClickScanQr() {
        Log.d(this.klass, "onClickScanQr")
    }
}