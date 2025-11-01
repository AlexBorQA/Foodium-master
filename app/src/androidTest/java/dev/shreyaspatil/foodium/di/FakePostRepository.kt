package dev.shreyaspatil.foodium.di

import dev.shreyaspatil.foodium.data.repository.PostRepository
import dev.shreyaspatil.foodium.data.repository.Resource
import dev.shreyaspatil.foodium.model.Post
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@ExperimentalCoroutinesApi
class FakePostRepository @Inject constructor() : PostRepository {
    override fun getAllPosts(): Flow<Resource<List<Post>>> {
        val posts = listOf(
            Post(1, "Title 1", "Author", "Body"),
            Post(2, "Title 2", "Author", "Body")
        )
        return flowOf(Resource.Success(posts))
    }

    override fun getPostById(postId: Int): Flow<Post> {
        return flowOf(Post(postId, "Title $postId", "Author", "Body"))
    }
}
