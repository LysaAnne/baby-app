package dk.babyapp.core.logging

import android.util.Log
import dk.babyapp.BuildConfig
import javax.inject.Inject

class AndroidAppLogger @Inject constructor() : AppLogger {
    override fun debug(event: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, event)
        }
    }

    private companion object {
        const val TAG = "BabyApp"
    }
}

