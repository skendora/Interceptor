package com.magicjack.interceptor.internal.data.repository

import androidx.lifecycle.LiveData
import com.magicjack.interceptor.internal.data.entity.HttpTransaction
import com.magicjack.interceptor.internal.data.entity.HttpTransactionTuple
import com.magicjack.interceptor.internal.data.room.InterceptorDatabase
import java.util.concurrent.Executor
import java.util.concurrent.Executors

internal class HttpTransactionDatabaseRepository(private val database: InterceptorDatabase) : HttpTransactionRepository {

    private val executor: Executor = Executors.newSingleThreadExecutor()

    private val transcationDao get() = database.transactionDao()

    override fun getFilteredTransactionTuples(code: String, path: String): LiveData<List<HttpTransactionTuple>> {
        val pathQuery = if (path.isNotEmpty()) "%$path%" else "%"
        return transcationDao.getFilteredTuples("$code%", pathQuery)
    }

    override fun getTransaction(transactionId: Long): LiveData<HttpTransaction> {
        return transcationDao.getById(transactionId)
    }

    override fun getSortedTransactionTuples(): LiveData<List<HttpTransactionTuple>> {
        return transcationDao.getSortedTuples()
    }

    override fun deleteAllTransactions() {
        executor.execute { transcationDao.deleteAll() }
    }

    override fun insertTransaction(transaction: HttpTransaction) {
        executor.execute {
            val id = transcationDao.insert(transaction)
            transaction.id = id ?: 0
        }
    }

    override fun updateTransaction(transaction: HttpTransaction): Int {
        return transcationDao.update(transaction)
    }

    override fun deleteOldTransactions(threshold: Long) {
        executor.execute { transcationDao.deleteBefore(threshold) }
    }
}
