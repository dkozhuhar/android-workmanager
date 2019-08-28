package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.R
import timber.log.Timber

class BlurWorker(val context: Context, blurWorkerParams: WorkerParameters) : Worker(context, blurWorkerParams) {
    override fun doWork(): Result {
        try {
            val picture = BitmapFactory.decodeResource(context.resources,R.drawable.test)
            val bluredPictureUri = writeBitmapToFile(context,blurBitmap(picture,context))
            makeStatusNotification(bluredPictureUri.toString(),context)
            return Result.success()
        } catch (t: Throwable) {
            makeStatusNotification(t.toString(),context)
            Timber.e(t)
            return Result.failure()
        }
    }

}