package com.adly.skycast.ui.home

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.adly.skycast.data.model.FavoriteLocationEntity
import com.adly.skycast.data.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    lateinit var application: Application

    @Mock
    lateinit var repository: WeatherRepository

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Ensure the ViewModel uses the mocked repository
        viewModel = HomeViewModel(application, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addFavorite should call repository insertFavorite`() = runTest {
        val favorite = FavoriteLocationEntity("Tokyo", "JP", 35.0, 139.0)

        // Mock the suspend function
        whenever(repository.insertFavorite(any())).thenAnswer { Unit }

        viewModel.addFavorite(favorite)

        // Verify the suspend function was called
        verify(repository).insertFavorite(favorite)
    }

    @Test
    fun `removeFavorite should call repository removeFavorite`() = runTest {
        val favorite = FavoriteLocationEntity("Cairo", "EG", 30.0, 31.0)

        // Mock the suspend function
        whenever(repository.removeFavorite(any())).thenAnswer { Unit }

        viewModel.removeFavorite(favorite)

        // Verify the suspend function was called
        verify(repository).removeFavorite(favorite)
    }
}