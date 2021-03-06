package com.magicjack.interceptor.internal.support

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.LongSparseArray
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.magicjack.interceptor.R
import com.magicjack.interceptor.api.Interceptor
import com.magicjack.interceptor.internal.data.entity.HttpTransaction
import com.magicjack.interceptor.internal.data.entity.RecordedThrowable
import com.magicjack.interceptor.internal.ui.BaseInterceptorActivity
import java.util.HashSet

internal class NotificationHelper(val context: Context) {

    companion object {
        private const val TRANSACTIONS_CHANNEL_ID = "interceptor_transactions"
        private const val ERRORS_CHANNEL_ID = "interceptor_errors"

        private const val TRANSACTION_NOTIFICATION_ID = 1138
        private const val ERROR_NOTIFICATION_ID = 3546

        private const val BUFFER_SIZE = 10
        private const val INTENT_REQUEST_CODE = 11
        private val transactionBuffer = LongSparseArray<HttpTransaction>()
        private val transactionIdsSet = HashSet<Long>()

        fun clearBuffer() {
            synchronized(transactionBuffer) {
                transactionBuffer.clear()
                transactionIdsSet.clear()
            }
        }
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val transactionsScreenIntent by lazy {
        PendingIntent.getActivity(
            context,
            TRANSACTION_NOTIFICATION_ID,
            Interceptor.getLaunchIntent(context, Interceptor.SCREEN_HTTP),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private val errorsScreenIntent by lazy {
        PendingIntent.getActivity(
            context,
            ERROR_NOTIFICATION_ID,
            Interceptor.getLaunchIntent(context, Interceptor.SCREEN_ERROR),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val transactionsChannel = NotificationChannel(
                TRANSACTIONS_CHANNEL_ID,
                context.getString(R.string.interceptor_networks_notification_category),
                NotificationManager.IMPORTANCE_LOW
            )
            val errorsChannel = NotificationChannel(
                ERRORS_CHANNEL_ID,
                context.getString(R.string.interceptor_errors_notification_category),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannels(listOf(transactionsChannel, errorsChannel))
        }
    }

    private fun addToBuffer(transaction: HttpTransaction) {
        if (transaction.id == 0L) {
            // Don't store Transactions with an invalid ID (0).
            // Transaction with an Invalid ID will be shown twice in the notification
            // with both the invalid and the valid ID and we want to avoid this.
            return
        }
        synchronized(transactionBuffer) {
            transactionIdsSet.add(transaction.id)
            transactionBuffer.put(transaction.id, transaction)
            if (transactionBuffer.size() > BUFFER_SIZE) {
                transactionBuffer.removeAt(0)
            }
        }
    }

    fun show(transaction: HttpTransaction) {
        addToBuffer(transaction)
        if (!BaseInterceptorActivity.isInForeground) {
            val builder =
                NotificationCompat.Builder(context, TRANSACTIONS_CHANNEL_ID)
                    .setContentIntent(transactionsScreenIntent)
                    .setLocalOnly(true)
                    .setSmallIcon(R.drawable.interceptor_ic_transaction_notification_24dp)
                    .setColor(ContextCompat.getColor(context, R.color.interceptor_color_primary))
                    .setContentTitle(context.getString(R.string.interceptor_http_notification_title))
                    .setAutoCancel(true)
                    .addAction(createClearAction(ClearDatabaseService.ClearAction.Transaction))
            val inboxStyle = NotificationCompat.InboxStyle()
            synchronized(transactionBuffer) {
                var count = 0
                (transactionBuffer.size() - 1 downTo 0).forEach { i ->
                    if (count < BUFFER_SIZE) {
                        if (count == 0) {
                            builder.setContentText(transactionBuffer.valueAt(i).notificationText)
                        }
                        inboxStyle.addLine(transactionBuffer.valueAt(i).notificationText)
                    }
                    count++
                }
                builder.setStyle(inboxStyle)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setSubText(transactionIdsSet.size.toString())
                } else {
                    builder.setNumber(transactionIdsSet.size)
                }
            }
            notificationManager.notify(TRANSACTION_NOTIFICATION_ID, builder.build())
        }
    }

    fun show(throwable: RecordedThrowable) {
        if (!BaseInterceptorActivity.isInForeground) {
            val builder =
                NotificationCompat.Builder(context, ERRORS_CHANNEL_ID)
                    .setContentIntent(errorsScreenIntent)
                    .setLocalOnly(true)
                    .setSmallIcon(R.drawable.interceptor_ic_error_notifications_24dp)
                    .setColor(ContextCompat.getColor(context, R.color.interceptor_status_error))
                    .setContentTitle(throwable.clazz)
                    .setAutoCancel(true)
                    .setContentText(throwable.message)
                    .addAction(createClearAction(ClearDatabaseService.ClearAction.Error))
            notificationManager.notify(ERROR_NOTIFICATION_ID, builder.build())
        }
    }

    private fun createClearAction(clearAction: ClearDatabaseService.ClearAction):
        NotificationCompat.Action {
            val clearTitle = context.getString(R.string.interceptor_clear)
            val deleteIntent = Intent(context, ClearDatabaseService::class.java).apply {
                putExtra(ClearDatabaseService.EXTRA_ITEM_TO_CLEAR, clearAction)
            }
            val intent = PendingIntent.getService(
                context, INTENT_REQUEST_CODE,
                deleteIntent, PendingIntent.FLAG_ONE_SHOT
            )
            return NotificationCompat.Action(R.drawable.interceptor_ic_delete_white_24dp, clearTitle, intent)
        }

    fun dismissTransactionsNotification() {
        notificationManager.cancel(TRANSACTION_NOTIFICATION_ID)
    }

    fun dismissErrorsNotification() {
        notificationManager.cancel(ERROR_NOTIFICATION_ID)
    }
}
