package spidgorny.whereismybus

import com.squareup.otto.Bus

class Globals {
    companion object {
        val instance = Globals()
    }
    private var bus: Bus? = null

    fun getBus(): Bus {
        if (this.bus != null) {
            return this.bus!!;
        }
        this.bus = Bus()
        return this.bus!!;
    }
}
