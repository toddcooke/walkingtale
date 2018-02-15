package com.android.example.github.repository.tasks

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.amazonaws.ClientConfiguration
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.android.example.github.vo.Resource
import com.android.example.github.vo.Status

abstract class AbstractTask<out I, O>(val input: I) : Runnable {

    val TAG = this.javaClass.simpleName
    val result = MutableLiveData<Resource<O>>()
    val dynamoDBClient: AmazonDynamoDBClient
    val dynamoDBMapper: DynamoDBMapper

    fun getResult(): LiveData<Resource<O>> {
        return result
    }

    init {
        dynamoDBClient = Region.getRegion(Regions.US_EAST_1)
                .createClient(
                        AmazonDynamoDBClient::class.java,
                        AWSMobileClient.getInstance().credentialsProvider,
                        ClientConfiguration())
        dynamoDBMapper = DynamoDBMapper(dynamoDBClient)
        result.postValue(Resource(Status.LOADING, null, null))
    }

    override fun run() {

    }
}