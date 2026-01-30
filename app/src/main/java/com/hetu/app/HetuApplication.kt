package com.hetu.app

import android.app.Application
import android.util.Log

/**
 * Hetu Application class
 * Note: SDK initialization is deferred to first use to avoid crashes
 */
class HetuApplication : Application() {
    
    companion object {
        private const val TAG = "HetuApp"
        
        @Volatile
        private var sdkInitialized = false
        
        fun ensureSDKInitialized() {
            if (!sdkInitialized) {
                try {
                    // Try to initialize SDK
                    com.runanywhere.sdk.public.RunAnywhere.initializeForDevelopment()
                    sdkInitialized = true
                    Log.i(TAG, "RunAnywhere SDK initialized successfully")
                } catch (e: UnsatisfiedLinkError) {
                    Log.e(TAG, "Native library not found: ${e.message}")
                    // SDK native libs might not be available
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize SDK: ${e.message}")
                }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "HetuApplication onCreate")
        
        // Don't initialize SDK here - defer to first use
        // This prevents crashes if native libs aren't properly loaded
    }
}
