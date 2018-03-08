package com.MapPost.repository.tasks

import com.MapPost.aws.getTransferUtility
import com.MapPost.db.WalkingTaleDb
import com.MapPost.vo.PostType
import com.MapPost.vo.Resource
import com.MapPost.vo.Status
import com.MapPost.vo.Story
import java.io.File

/**
 * Uploads a file to S3
 * */
class PutFileS3Task(private val s3Args: S3Args, val db: WalkingTaleDb) :
        AbstractTask<S3Args, Story>(s3Args, db) {

    override fun run() {
        val transferUtility = getTransferUtility(s3Args.context)

        val expositions = s3Args.story.chapters
                .flatMap { it.posts }
                .filter { it.type == PostType.AUDIO || it.type == PostType.PICTURE }

        // Upload posts
        expositions.forEach {
            val s3Path = "${s3Args.story.id}/${it.postId}"
            transferUtility.upload(s3Path, File(it.content))
            // Change posts from local paths to s3 paths
            it.content = s3Path
        }

        // Upload story image
        transferUtility.upload("${s3Args.story.id}/story_image", File(s3Args.story.story_image))

        // Change story image path
        s3Args.story.story_image = "${s3Args.story.id}/story_image"

        result.postValue(Resource(Status.SUCCESS, s3Args.story, null))
    }
}