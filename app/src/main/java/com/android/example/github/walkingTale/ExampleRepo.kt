package com.android.example.github.walkingTale

import com.android.example.github.vo.Repo
import java.util.*

/**
 * 11/15/17.
 */
class ExampleRepo() {
    companion object {
        fun getRepo(): Repo {
            return Repo(
                    1, "name", "description", mutableListOf(), "genre",
                    "tags", "duration", 1.1, 1.1, 1.1,
                    "story_image"
            )
        }

        fun getRandomRepo(): Repo {
            val repo = getRepo()
            repo.id = Random().nextInt()
            return repo
        }
    }
}