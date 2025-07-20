package net.alunando.dormindo.di

import android.content.Context
import androidx.work.WorkManager
import net.alunando.dormindo.data.datasource.MediaSessionDataSource
import net.alunando.dormindo.data.datasource.NotificationDataSource
import net.alunando.dormindo.data.datasource.WorkManagerDataSource
import net.alunando.dormindo.data.repository.MediaRepositoryImpl
import net.alunando.dormindo.data.repository.SettingsRepositoryImpl
import net.alunando.dormindo.data.repository.TimerRepositoryImpl
import net.alunando.dormindo.domain.repository.MediaRepository
import net.alunando.dormindo.domain.repository.SettingsRepository
import net.alunando.dormindo.domain.repository.TimerRepository
import net.alunando.dormindo.domain.usecase.GetTimerStatusUseCase
import net.alunando.dormindo.domain.usecase.StartTimerUseCase
import net.alunando.dormindo.domain.usecase.StopTimerUseCase
import net.alunando.dormindo.presentation.viewmodel.TimerViewModel
import net.alunando.dormindo.R
import org.koin.android.ext.koin.androidApplication
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
    factory { StartTimerUseCase(get(), get(), get<Context>().getString(R.string.error_no_media_playing_short)) }
    factory { StopTimerUseCase(get(), get()) }
    factory { GetTimerStatusUseCase(get()) }
    
    // ViewModels
    viewModel {
        TimerViewModel(
            application = androidApplication(),
            startTimerUseCase = get<StartTimerUseCase>(),
            stopTimerUseCase = get<StopTimerUseCase>(),
            getTimerStatusUseCase = get<GetTimerStatusUseCase>(),
            mediaRepository = get<MediaRepository>(),
            timerRepository = get<TimerRepository>()
        )
    }
} 