package com.siddharth.practiceapp.worker


import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.siddharth.practiceapp.api.RetrofitInstance.Companion.api
import com.siddharth.practiceapp.data.entities.News.Article
import com.siddharth.practiceapp.data.entities.News.News
import com.siddharth.practiceapp.util.sendNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.lang.Integer.min
import java.net.URL
import kotlin.random.Random

/**
 * This Worker fetches top headline from the NewsApi every 15 minutes.
 */

class MyWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val TAG = "MyWorker : "

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                fetchDataUsingCoroutine()
            }
            Result.success()
        } catch (throwable: Throwable) {
            Log.d(TAG, "Error")
            Result.failure()
        }
    }

    /**
     * This function fetches the top news from the api using coroutine and
     * Shows the fetched news as Notification using showNotification().
     */

    private suspend fun fetchDataUsingCoroutine() {
        val rawData = api.getTopNewsUsingCoroutine()
        Log.d(TAG, rawData.isSuccessful.toString())
        val results = rawData.body()
        val index =  (0..5).random()
        val news = results?.articles?.get(index)
        news?.let {
            showNotification(news)
        }
    }


    /**
     * This function fetches the top news from the api using thread class and
     * Shows the fetched news as Notification using showNotification().
     */
    private fun fetchPostsUsingThread() {
        var rawData: Response<News>? = null
        val thread = Thread {
            //code to do the HTTP request
            rawData = api.getTopNewsUsingThread()
        }
        thread.start()
        Log.d(TAG, rawData?.isSuccessful.toString())
        val results = rawData?.body()
        // fetching the title of first article from results.
        val news = results?.articles?.get(0)
        news?.let {
            showNotification(news)
        }
    }

    private fun showNotification(article: Article) {
        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager

        val url = URL(article.urlToImage)
        try {
            val bitmap: Bitmap? = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            notificationManager.sendNotification(
                "Here's your Daily News",
                article.title,
                article.content,
                bitmap,
                applicationContext
            )
        } catch (e: IOException) {

        }
    }
}