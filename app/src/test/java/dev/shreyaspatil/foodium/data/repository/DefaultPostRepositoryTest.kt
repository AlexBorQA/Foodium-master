/*
 * MIT License
 */

package dev.shreyaspatil.foodium.data.repository

import com.google.common.truth.Truth.assertThat
import dev.shreyaspatil.foodium.data.local.dao.PostsDao
import dev.shreyaspatil.foodium.data.remote.api.FoodiumService
import dev.shreyaspatil.foodium.model.Post
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class DefaultPostRepositoryTest {

    private class FakeDao : PostsDao {
        private val posts = mutableListOf<Post>()
        override suspend fun addPosts(posts: List<Post>) { this.posts.clear(); this.posts.addAll(posts) }
        override suspend fun deleteAllPosts() { posts.clear() }
        override fun getPostById(postId: Int) = kotlinx.coroutines.flow.flow { emit(posts.first { it.id == postId }) }
        override fun getAllPosts() = kotlinx.coroutines.flow.flow { emit(posts.toList()) }
    }

    private class FakeService(private val payload: List<Post>) : FoodiumService {
        override suspend fun getPosts(): Response<List<Post>> = Response.success(payload)
    }

    @Test
    fun getAllPosts_emitsLocalThenRemoteSaved() = runBlocking {
        val initial = listOf<Post>()
        val remote = listOf(Post(1, "t", "a", "b", imageUrl = ""))
        val dao = FakeDao().apply { addPosts(initial) }
        val service = FakeService(remote)

        val repo = DefaultPostRepository(dao, service)

        val states = mutableListOf<Resource<List<Post>>>()
        repo.getAllPosts().take(2).toList(states)

        // First emission from local (empty), then after remote save, local again with 1 item
        assertThat(states.size).isAtLeast(2)
        assertThat((states.first() as Resource.Success).data).isEmpty()
        assertThat((states.last() as Resource.Success).data).hasSize(1)
    }
}


