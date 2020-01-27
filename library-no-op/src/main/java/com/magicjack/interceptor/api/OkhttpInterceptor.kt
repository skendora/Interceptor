package com.magicjack.interceptor.api

import android.content.Context
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * No-op implementation.
 */
class OkhttpInterceptor @JvmOverloads constructor(
    context: Context,
    collector: Any? = null,
    maxContentLength: Any? = null,
    headersToRedact: Any? = null
) : Interceptor {

    fun redactHeaders(vararg names: String): OkhttpInterceptor {
        return this
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return chain.proceed(request)
    }
}
