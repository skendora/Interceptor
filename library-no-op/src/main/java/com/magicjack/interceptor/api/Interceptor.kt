package com.magicjack.interceptor.api

import android.content.Context
import android.content.Intent

/**
 * No-op implementation.
 */
object Interceptor {

    const val SCREEN_HTTP = 1
    const val SCREEN_ERROR = 2


    const val isOp = false

    @JvmStatic
    fun getLaunchIntent(context: Context, screen: Int): Intent {
        return Intent()
    }

    @JvmStatic
    fun registerDefaultCrashHandler(collector: InterceptorCollector) {
        // Empty method for the library-no-op artifact
    }

    @JvmStatic
    fun dismissTransactionsNotification(context: Context) {
        // Empty method for the library-no-op artifact
    }

    @JvmStatic
    fun dismissErrorsNotification(context: Context) {
        // Empty method for the library-no-op artifact
    }
}
