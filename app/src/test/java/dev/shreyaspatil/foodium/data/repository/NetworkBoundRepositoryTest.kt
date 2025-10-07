/*
 * MIT License
 */

package dev.shreyaspatil.foodium.data.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import org.junit.Test

@ExperimentalCoroutinesApi
class NetworkBoundRepositoryTest {

    @Test
    fun emitsErrorWhenRemoteFails() = runBlocking {
        val repo = object : NetworkBoundRepository<List<Int>, List<Int>>() {
            override suspend fun saveRemoteData(response: List<Int>) { /* no-op */ }
            override fun fetchFromLocal(): Flow<List<Int>> = flow { emit(emptyList()) }
            override suspend fun fetchFromRemote(): Response<List<Int>> = Response.error(500, okhttp3.ResponseBody.create(null, ""))
        }

        val out = mutableListOf<Resource<List<Int>>>()
        repo.asFlow().take(2).toList(out)
        assertThat(out[1] is Resource.Failed).isTrue()
    }
}





