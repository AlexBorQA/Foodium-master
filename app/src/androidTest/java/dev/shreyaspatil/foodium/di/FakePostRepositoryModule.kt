package dev.shreyaspatil.foodium.di

import dagger.Binds
import dagger.Module
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.testing.TestInstallIn
import dev.shreyaspatil.foodium.data.repository.PostRepository
import dev.shreyaspatil.foodium.di.module.PostRepositoryModule
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Module
@TestInstallIn(
    components = [ActivityRetainedComponent::class],
    replaces = [PostRepositoryModule::class]
)
abstract class FakePostRepositoryModule {
    @Binds
    @ActivityRetainedScoped
    abstract fun bindPostRepository(impl: FakePostRepository): PostRepository
}
