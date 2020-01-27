package com.magicjack.interceptor.api

import android.content.Context
import android.content.Intent
import androidx.annotation.IntDef
import com.magicjack.interceptor.internal.support.InterceptorCrashHandler
import com.magicjack.interceptor.internal.support.NotificationHelper
import com.magicjack.interceptor.internal.ui.MainActivity

/**
 * Interceptor methods and utilities to interact with the library.
 */
object Interceptor {

    const val SCREEN_HTTP = 1
    const val SCREEN_ERROR = 2

    /**
     * Check if this instance is the operation one or no-op.
     * @return `true` if this is the operation instance.
     */

    const val isOp = true

    /**
     * Get an Intent to launch the Interceptor UI directly.
     * @param context An Android [Context].
     * @param screen The [Screen] to display: SCREEN_HTTP or SCREEN_ERROR.
     * @return An Intent for the main Interceptor Activity that can be started with [Context.startActivity].
     */
    @JvmStatic
    fun getLaunchIntent(context: Context, @Screen screen: Int): Intent {
        return Intent(context, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(MainActivity.EXTRA_SCREEN, screen)
    }

    /**
     * Configure the default crash handler of the JVM to report all uncaught [Throwable] to Interceptor.
     * You may only use it for debugging purpose.
     *
     * @param collector the InterceptorCollector
     */
    @JvmStatic
    fun registerDefaultCrashHandler(collector: InterceptorCollector) {
        Thread.setDefaultUncaughtExceptionHandler(InterceptorCrashHandler(collector))
    }

    /**
     * Method to dismiss the Interceptor notification of HTTP Transactions
     */
    @JvmStatic
    fun dismissTransactionsNotification(context: Context) {
        NotificationHelper(context).dismissTransactionsNotification()
    }

    /**
     * Method to dismiss the Interceptor notification of Uncaught Errors.
     */
    @JvmStatic
    fun dismissErrorsNotification(context: Context) {
        NotificationHelper(context).dismissErrorsNotification()
    }

    /**
     * Annotation used to specify which screen of Interceptor should be launched.
     */
    @IntDef(value = [SCREEN_HTTP, SCREEN_ERROR])
    annotation class Screen

    internal const val LOG_TAG = "Interceptor"
}
