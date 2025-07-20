package com.example.dormindo

import android.app.Application
import com.example.dormindo.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Classe Application para inicializar o Koin
 */
class DormindoApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializa o Koin
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@DormindoApplication)
            modules(appModule)
        }
    }
} 