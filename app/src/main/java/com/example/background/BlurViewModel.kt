/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.work.*
import com.example.background.workers.BlurWorker
import com.example.background.workers.CleanupWorker
import com.example.background.workers.SaveImageToFileWorker


class BlurViewModel(application: Application) : AndroidViewModel(application) {

    internal var imageUri: Uri? = null
    internal var outputUri: Uri? = null



    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    /**
     * Setters
     */
    internal fun setImageUri(uri: String?) {
        imageUri = uriOrNull(uri)
    }

    internal fun setOutputUri(outputImageUri: String?) {
        outputUri = uriOrNull(outputImageUri)
    }

    private val blurWorkManager = WorkManager.getInstance(application)

    private val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .build()

    val outputWorkInfos = blurWorkManager.getWorkInfosByTagLiveData(TAG_OUTPUT)

    fun blur(blurLevel: Int)  {
        val inputData = Data.Builder().putString(KEY_IMAGE_URI,imageUri.toString()).build()
        val cleanupRequest = OneTimeWorkRequestBuilder<CleanupWorker>().build()
        val saveRequest = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
                .addTag(TAG_OUTPUT)
                .setConstraints(constraints)
                .build()
        var continuation = blurWorkManager.beginUniqueWork(IMAGE_MANIPULATION_WORK_NAME,ExistingWorkPolicy.REPLACE,cleanupRequest)
        for (i in 1..blurLevel) {
            if (i == 1) {
                continuation = continuation.then(OneTimeWorkRequestBuilder<BlurWorker>().setInputData(inputData).build())
            } else
                continuation = continuation.then(OneTimeWorkRequestBuilder<BlurWorker>().build())
        }
        continuation.then(saveRequest)
                .enqueue()
    }

    internal fun cancelWork() {
        blurWorkManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
    }

}
