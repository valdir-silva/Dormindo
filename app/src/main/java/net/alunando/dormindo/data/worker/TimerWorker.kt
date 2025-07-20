package net.alunando.dormindo.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import net.alunando.dormindo.data.datasource.MediaSessionDataSource
import kotlinx.coroutines.delay

/**
 * Worker para executar o timer em background
 */
class TimerWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "TimerWorker"
    }
    
    override suspend fun doWork(): Result {
        return try {
            val durationMinutes = inputData.getLong("duration_minutes", 0)
            
            if (durationMinutes <= 0) {
                Log.w(TAG, "Duração inválida: $durationMinutes minutos")
                return Result.failure()
            }
            
            Log.d(TAG, "Iniciando timer por $durationMinutes minutos")
            
            // Aguarda o tempo definido
            val durationMillis = durationMinutes * 60 * 1000
            delay(durationMillis)
            
            // Para a reprodução de mídia
            val mediaDataSource = MediaSessionDataSource(context)
            val stopResult = mediaDataSource.stopMediaPlayback()
            
            if (stopResult.isSuccess) {
                Log.d(TAG, "Timer concluído - reprodução de mídia parada")
                Result.success()
            } else {
                Log.e(TAG, "Erro ao parar reprodução de mídia: ${stopResult.exceptionOrNull()}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro durante execução do timer", e)
            Result.failure()
        }
    }
} 