package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import com.example.background.R
import timber.log.Timber
import java.lang.IllegalArgumentException

class BlurWorker(val context: Context, blurWorkerParams: WorkerParameters) : Worker(context, blurWorkerParams) {
    override fun doWork(): Result {
        val inputUri = inputData.getString(KEY_IMAGE_URI)
        return try {
            if (TextUtils.isEmpty(inputUri)) {
                throw IllegalArgumentException("Invalid input Uri: $inputUri")
            }
//            val picture = BitmapFactory.decodeResource(context.applicationContext.resources, R.drawable.test)
            val picture = BitmapFactory.decodeStream(context.contentResolver.openInputStream(Uri.parse(inputUri)))
            val bluredPictureUri = writeBitmapToFile(context, blurBitmap(picture, context))
//            val outputData = Data.Builder().putString(KEY_IMAGE_URI,bluredPictureUri.toString()).build()
            makeStatusNotification(bluredPictureUri.toString(), context)

            Result.success(workDataOf(KEY_IMAGE_URI to bluredPictureUri.toString()))
        } catch (t: Throwable) {
            makeStatusNotification(t.toString(), context)
            Timber.e(t)
            Result.failure()
        }
    }

}