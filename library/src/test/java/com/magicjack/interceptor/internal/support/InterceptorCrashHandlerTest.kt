package com.magicjack.interceptor.internal.support

import com.magicjack.interceptor.api.InterceptorCollector
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class InterceptorCrashHandlerTest {

    @Test
    fun uncaughtException_isReportedCorrectly() {
        val mockCollector = mockk<InterceptorCollector>()
        val mockThrowable = Throwable()
        val handler = InterceptorCrashHandler(mockCollector)
        every { mockCollector.onError(any(), any()) } returns Unit

        handler.uncaughtException(Thread.currentThread(), mockThrowable)

        verify { mockCollector.onError("Error caught on ${Thread.currentThread().name} thread", mockThrowable) }
    }
}
