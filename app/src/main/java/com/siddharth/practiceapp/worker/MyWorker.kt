package com.siddharth.practiceapp.worker


import android.app.NotificationManager
import  com.siddharth.practiceapp.util.sendNotification
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.siddharth.practiceapp.api.RetrofitInstance.Companion.api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext,workerParams) {

    private val TAG = "MyWorker : "

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO){
                fetchData()
            }
            Result.success()
        } catch (throwable: Throwable) {
            Log.d(TAG, "Error")
            showNotification("Oops! something went wrong")
            Result.failure()
        }
    }

    private suspend fun fetchData() {
            val rawData = api.getTopNews()
            Log.d(TAG,rawData.isSuccessful.toString())
            val results = rawData.body()
            val news = results?.articles?.get(0)?.title
            news?.let{
                showNotification(news)
            }
    }

    private fun showNotification(message : String) {
        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager

        notificationManager.sendNotification(
            "Breaking News",
            message,
            applicationContext
        )
    }
}