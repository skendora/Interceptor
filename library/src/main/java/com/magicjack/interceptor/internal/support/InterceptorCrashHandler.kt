package com.magicjack.interceptor.internal.support

import com.magicjack.interceptor.api.InterceptorCollector

internal class InterceptorCrashHandler(private val collector: InterceptorCollector) : Thread.UncaughtExceptionHandler {

    private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        collector.onError("Error caught on ${thread.name} thread", throwable)
        defaultHandler?.uncaughtException(thread, throwable)
    }
}
