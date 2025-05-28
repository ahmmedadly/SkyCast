package com.adly.skycast.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adly.skycast.data.local_source.WeatherDao
import com.adly.skycast.data.model.FavoriteLocationEntity
import com.adly.skycast.data.remote_source.WeatherApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class WeatherRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    @Mock
    lateinit var weatherApiService: WeatherApiService

    @Mock
    lateinit var weatherDao: WeatherDao

    private lateinit var repository: WeatherRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = WeatherRepository(weatherApiService, weatherDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `getFavorites should return DAO LiveData`() {
        val fakeLiveData = MutableLiveData<List<FavoriteLocationEntity>>()
        Mockito.`when`(weatherDao.getAllFavorites()).thenReturn(fakeLiveData)

        val result = repository.getFavorites()

        assert(result === fakeLiveData)
        Mockito.verify(weatherDao).getAllFavorites()
    }

    @Test
    fun `removeFavorite should call DAO delete`() = runBlockingTest {
        val favorite = FavoriteLocationEntity("Paris", "FR", 48.8566, 2.3522)

        repository.removeFavorite(favorite)

        Mockito.verify(weatherDao).deleteFavorite(favorite)
    }
}
