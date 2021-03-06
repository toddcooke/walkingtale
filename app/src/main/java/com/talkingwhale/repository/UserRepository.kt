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

package com.talkingwhale.repository

import android.arch.lifecycle.LiveData
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression
import com.talkingwhale.pojos.Resource
import com.talkingwhale.pojos.Status
import com.talkingwhale.pojos.User
import com.talkingwhale.util.AppExecutors

/**
 * Repository that handles User objects.
 */
object UserRepository {
    private val appExecutors: AppExecutors = AppExecutors

    fun loadUser(userId: String): LiveData<Resource<User>> {
        val result = object : AbstractTask<User>() {
            override fun run() {
                try {
                    val user = User()
                    user.userId = userId
                    val query = DynamoDBQueryExpression<User>().withHashKeyValues(user)
                    val response = dynamoDBMapper.query(User::class.java, query).first { it.userId == userId }
                    result.postValue(Resource(Status.SUCCESS, response, ""))
                } catch (e: NoSuchElementException) {
                    result.postValue(Resource(Status.ERROR, null, ""))
                }
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }

    fun putUser(user: User): LiveData<Resource<Unit>> {
        val result = object : AbstractTask<Unit>() {
            override fun run() {
                result.postValue(Resource(Status.SUCCESS, dynamoDBMapper.save(user), ""))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }

    fun deleteUser(user: User): LiveData<Resource<Unit>> {
        val result = object : AbstractTask<Unit>() {
            override fun run() {
                dynamoDBMapper.delete(user)
                result.postValue(Resource(Status.SUCCESS, Unit, ""))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }
}
