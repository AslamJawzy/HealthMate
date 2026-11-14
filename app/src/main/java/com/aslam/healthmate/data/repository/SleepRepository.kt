package com.aslam.healthmate.data.repository

import com.aslam.healthmate.data.db.SleepLog
import com.aslam.healthmate.data.db.SleepLogDao
import kotlinx.coroutines.flow.Flow

class SleepRepository(private val dao: SleepLogDao) {

    val allLogs: Flow<List<SleepLog>> = dao.getAll()

    suspend fun getByDate(date: String): SleepLog? = dao.getByDate(date)

    suspend fun add(log: SleepLog) = dao.insert(log)

    suspend fun delete(log: SleepLog) = dao.delete(log)

    suspend fun addSampleLogsIfEmpty(logs: List<SleepLog>) {
        if (dao.count() == 0) dao.insertAll(logs)
    }
}
