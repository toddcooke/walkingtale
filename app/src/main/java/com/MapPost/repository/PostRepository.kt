/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.MapPost.repository

import android.arch.lifecycle.LiveData
import android.content.Context
import android.util.Log
import com.MapPost.AppExecutors
import com.MapPost.repository.tasks.AbstractTask
import com.MapPost.vo.Post
import com.MapPost.vo.Resource
import com.MapPost.vo.Status
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions
import com.s3BucketName
import java.io.File
import java.lang.Exception


object PostRepository {

    private val tag = "PostRepository"
    private val appExecutors: AppExecutors = AppExecutors

    fun getNearbyPosts(): LiveData<Resource<List<Post>>> {
        val result = object : AbstractTask<String, List<Post>>("") {
            override fun run() {
                result.postValue(Resource(Status.SUCCESS, dynamoDBMapper.scan(Post::class.java, DynamoDBScanExpression()), ""))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }

    fun addPost(post: Post): LiveData<Resource<Unit>> {
        val result = object : AbstractTask<Post, Unit>(post) {
            override fun run() {
                result.postValue(Resource(Status.SUCCESS, dynamoDBMapper.save(post), ""))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }

    fun putFile(pair: Pair<Post, Context>): LiveData<Resource<Post>> {
        val result = object : AbstractTask<Pair<Post, Context>, Post>(pair) {
            override fun run() {
                val post = pair.first
                val transferUtility = getTransferUtility(pair.second)
                val s3Path = post.postId
                transferUtility.upload(s3Path, File(post.content)).setTransferListener(object : TransferListener {
                    override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                        Log.i(tag, "" + bytesCurrent)
                    }

                    override fun onStateChanged(id: Int, state: TransferState?) {
                        Log.i(tag, "" + state)
                    }

                    override fun onError(id: Int, ex: Exception?) {
                        Log.i(tag, "" + ex)
                    }
                })
                post.content = s3Path
                result.postValue(Resource(Status.SUCCESS, post, null))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }

    fun deletePost(post: Post): LiveData<Resource<Unit>> {
        val result = object : AbstractTask<Post, Unit>(post) {
            override fun run() {
                result.postValue(Resource(Status.SUCCESS, dynamoDBMapper.delete(post), ""))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }

    fun getTransferUtility(context: Context): TransferUtility {
        val amazonS3 = AmazonS3Client(AWSMobileClient.getInstance().credentialsProvider)
        amazonS3.setS3ClientOptions(S3ClientOptions.builder().disableChunkedEncoding().build())
        return TransferUtility.builder()
                .defaultBucket(s3BucketName)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .s3Client(amazonS3)
                .context(context)
                .build()
    }
}
