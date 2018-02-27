package com.walkingtale.repository.tasks

import com.walkingtale.aws.getTransferUtility
import com.walkingtale.db.WalkingTaleDb
import com.walkingtale.vo.ExpositionType
import com.walkingtale.vo.Resource
import com.walkingtale.vo.Status
import com.walkingtale.vo.Story
import java.io.File

/**
 * Uploads a file to S3
 * */
class PutFileS3Task(private val s3Args: S3Args, val db: WalkingTaleDb) :
        AbstractTask<S3Args, Story>(s3Args, db) {

    override fun run() {
        val transferUtility = getTransferUtility(s3Args.context)

        val expositions = s3Args.story.chapters
                .flatMap { it.expositions }
                .filter { it.type == ExpositionType.AUDIO || it.type == ExpositionType.PICTURE }

        // Upload expositions
        expositions.forEach {
            val s3Path = "${s3Args.story.id}/${it.id}"
            transferUtility.upload(s3Path, File(it.content))
            // Change expositions from local paths to s3 paths
            it.content = s3Path
        }

        // Upload story image
        transferUtility.upload("${s3Args.story.id}/story_image", File(s3Args.story.story_image))

        // Change story image path
        s3Args.story.story_image = "${s3Args.story.id}/story_image"

        result.postValue(Resource(Status.SUCCESS, s3Args.story, null))
    }
}