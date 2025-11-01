/*
 * MIT License
 */

package dev.shreyaspatil.foodium.ui.main

import com.google.common.truth.Truth.assertThat
import dev.shreyaspatil.foodium.data.repository.PostRepository
import dev.shreyaspatil.foodium.model.Post
import dev.shreyaspatil.foodium.model.State
import dev.shreyaspatil.foodium.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Rule

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val mainRule = MainCoroutineRule()

    private class FakeRepo(private val flow: Flow<dev.shreyaspatil.foodium.data.repository.Resource<List<Post>>>) : PostRepository {
        override fun getAllPosts() = flow
        override fun getPostById(postId: Int) = flowOf(Post(postId, "t", "a", "b", ""))
    }

    @Test
    fun getPosts_emitsLoadingThenSuccess() = runBlocking {
        val posts = listOf(Post(1, "t", "a", "b", ""))
        val resourceFlow = MutableSharedFlow<dev.shreyaspatil.foodium.data.repository.Resource<List<Post>>>(replay = 1)

        val vm = MainViewModel(FakeRepo(resourceFlow))
        // Initially loading
        assertThat(vm.posts.value.isLoading()).isTrue()

        vm.getPosts()
        resourceFlow.emit(dev.shreyaspatil.foodium.data.repository.Resource.Success(posts))

        Thread.sleep(100)
        val value = vm.posts.value
        assertThat(value is State.Success).isTrue()
        assertThat((value as State.Success).data).hasSize(1)
    }
}


