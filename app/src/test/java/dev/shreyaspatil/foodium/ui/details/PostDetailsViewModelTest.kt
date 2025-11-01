/*
 * MIT License
 */

package dev.shreyaspatil.foodium.ui.details

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.common.truth.Truth.assertThat
import dev.shreyaspatil.foodium.data.repository.PostRepository
import dev.shreyaspatil.foodium.data.repository.Resource
import dev.shreyaspatil.foodium.model.Post
import dev.shreyaspatil.foodium.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class PostDetailsViewModelTest {

    @get:Rule
    val instant = InstantTaskExecutorRule()

    @get:Rule
    val mainRule = MainCoroutineRule()

    private class FakeRepo : PostRepository {
        override fun getAllPosts() = flowOf(Resource.Success(emptyList<Post>()))
        override fun getPostById(postId: Int) = flowOf(Post(postId, "t", "a", "b", ""))
    }

    @Test
    fun exposesPostFromRepository() {
        val vm = PostDetailsViewModel(FakeRepo(), 7)
        val value = vm.post.getOrAwaitValue(5)
        assertThat(value).isNotNull()
        assertThat(value!!.id).isEqualTo(7)
    }
}

private fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    unit: TimeUnit = TimeUnit.SECONDS
): T? {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(t: T) {
            data = t
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    this.observeForever(observer)
    latch.await(time, unit)
    return data
}


