package com.MapPost.vo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.support.annotation.NonNull
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.clustering.ClusterItem


@DynamoDBTable(tableName = "mappost-mobilehub-452475001-Posts")
@Entity(indices = [(Index("userId"))], primaryKeys = ["userId"])
data class Post(
        @get:DynamoDBHashKey(attributeName = "userId")
        @get:DynamoDBAttribute(attributeName = "userId")
        @NonNull
        var userId: String,
        @get:DynamoDBRangeKey(attributeName = "postId")
        @get:DynamoDBAttribute(attributeName = "postId")
        var postId: String,
        @get:DynamoDBAttribute(attributeName = "dateTime")
        var dateTime: String,
        @get:DynamoDBIndexHashKey(attributeName = "latitude", globalSecondaryIndexName = "latitude")
        @get:DynamoDBIndexRangeKey(attributeName = "latitude", globalSecondaryIndexName = "longitude")
        var latitude: Double,
        @get:DynamoDBIndexHashKey(attributeName = "longitude", globalSecondaryIndexName = "longitude")
        @get:DynamoDBIndexRangeKey(attributeName = "longitude", globalSecondaryIndexName = "latitude")
        var longitude: Double,
        @get:DynamoDBAttribute(attributeName = "tags")
        var tags: MutableList<String>,
        @get:DynamoDBAttribute(attributeName = "type")
        @DynamoDBMarshalling(marshallerClass = PostTypeMarshaller::class)
        var type: PostType,
        @get:DynamoDBAttribute(attributeName = "content")
        var content: String,
        @get:DynamoDBAttribute(attributeName = "linkedPosts")
        var linkedPosts: MutableList<String>
) : ClusterItem {
    @DynamoDBIgnore
    override fun getSnippet(): String {
        return ""
    }

    @DynamoDBIgnore
    override fun getTitle(): String {
        return ""
    }

    @DynamoDBIgnore
    override fun getPosition(): LatLng {
        return LatLng(latitude, longitude)
    }

    /**
     * Do not use this constructor!
     * Just to make DDB mapper happy
     * */
    constructor() : this("", "", "", 0.0, 0.0, mutableListOf(), PostType.TEXT, "", mutableListOf())
}

class PostTypeMarshaller : DynamoDBMarshaller<PostType> {
    override fun unmarshall(clazz: Class<PostType>?, obj: String?): PostType {
        return Gson().fromJson(obj, clazz)
    }

    override fun marshall(getterReturnResult: PostType?): String {
        return Gson().toJson(getterReturnResult)
    }
}

enum class PostType {
    TEXT, AUDIO, PICTURE, VIDEO
}