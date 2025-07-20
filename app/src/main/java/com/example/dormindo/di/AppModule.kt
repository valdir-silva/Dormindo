package com.example.dormindo.di

import android.content.Context
import androidx.work.WorkManager
import com.example.dormindo.data.datasource.MediaSessionDataSource
import com.example.dormindo.data.datasource.NotificationDataSource
import com.example.dormindo.data.datasource.WorkManagerDataSource
import com.example.dormindo.data.repository.MediaRepositoryImpl
import com.example.dormindo.data.repository.SettingsRepositoryImpl
import com.example.dormindo.data.repository.TimerRepositoryImpl
import com.example.dormindo.domain.repository.MediaRepository
import com.example.dormindo.domain.repository.SettingsRepository
import com.example.dormindo.domain.repository.TimerRepository
import com.example.dormindo.domain.usecase.GetTimerStatusUseCase
import com.example.dormindo.domain.usecase.StartTimerUseCase
import com.example.dormindo.domain.usecase.StopTimerUseCase
import com.example.dormindo.presentation.viewmodel.TimerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Módulo principal de injeção de dependência
 */
val appModule = module {
    
    // System Services
    single { WorkManager.getInstance(get<Context>()) }
    
    // Data Sources
    single { MediaSessionDataSource(get()) }
    single { WorkManagerDataSource(get()) }
    single { NotificationDataSource(get()) }
    
    // Repositories
    single<TimerRepository> { 
        TimerRepositoryImpl(
            workManagerDataSource = get(),
            notificationDataSource = get()
        ) 
    }
    single<MediaRepository> { MediaRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl() }
    
    // Use Cases
    factory { StartTimerUseCase(get(), get()) }
    factory { StopTimerUseCase(get(), get()) }
    factory { GetTimerStatusUseCase(get()) }
    
    // ViewModels
    viewModel { 
        TimerViewModel(
            startTimerUseCase = get<StartTimerUseCase>(),
            stopTimerUseCase = get<StopTimerUseCase>(),
            getTimerStatusUseCase = get<GetTimerStatusUseCase>(),
            mediaRepository = get<MediaRepository>(),
            timerRepository = get<TimerRepository>()
        ) 
    }
} 