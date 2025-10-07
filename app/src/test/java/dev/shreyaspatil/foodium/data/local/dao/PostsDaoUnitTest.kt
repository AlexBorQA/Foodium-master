/*
 * MIT License
 */

package dev.shreyaspatil.foodium.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.google.common.truth.Truth.assertThat
import dev.shreyaspatil.foodium.data.local.FoodiumPostsDatabase
import dev.shreyaspatil.foodium.model.Post
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PostsDaoUnitTest {

    @get:Rule
    val instant = InstantTaskExecutorRule()

    private lateinit var db: FoodiumPostsDatabase
    private lateinit var dao: PostsDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FoodiumPostsDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.getPostsDao()
    }

    @After
    fun teardown() { db.close() }

    @Test
    fun addAndQueryPosts() = runBlocking {
        val posts = listOf(Post(1, "t", "a", "b", ""))
        dao.addPosts(posts)
        val all = dao.getAllPosts().first()
        assertThat(all).hasSize(1)
        val byId = dao.getPostById(1).first()
        assertThat(byId.title).isEqualTo("t")
    }
}


