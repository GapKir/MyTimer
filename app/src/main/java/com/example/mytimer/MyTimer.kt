package com.example.mytimer

import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

class MyTimer {
    private var counter = AtomicInteger(DEFAULT_TIMER_VALUE)
    private val handler = Handler(Looper.getMainLooper())
    private val backgroundThread = BackgroundThread()
    private var customTimePattern = null ?: DEFAULT_TIME_PATTERN
    private val lock = ReentrantLock()
    private var isTimerStarted = false
    private var callback: ((String) -> Unit)? = null

    fun getDefaultValue(): String {
        return formattingDate()
    }

    fun startTimer(callback: (String) -> Unit, timePattern: String? = null) {
        if (isTimerStarted) return
        this.callback = callback
        isTimerStarted = true
        timePattern?.let { this.customTimePattern = it }
        progressTimer()
    }

    fun pauseTimer() {
        if (isTimerStarted) {
            isTimerStarted = false
            counter.getAndIncrement()
        }
    }

    fun resetTimer() {
        isTimerStarted = false
        backgroundThread.executeTask {
            lock.lock()
            try {
                counter.set(DEFAULT_TIMER_VALUE)
                handler.post {
                    callback?.invoke(formattingDate())
                }
            } finally {
                lock.unlock()
            }
        }
    }

    private fun progressTimer() {
        backgroundThread.executeTask {
            for (tick in counter.get() downTo 0) {
                if (!isTimerStarted) return@executeTask
                try {
                    lock.lock()
                    TimeUnit.SECONDS.sleep(TICK_INTERVAL)
                    counter.decrementAndGet()
                    handler.post {
                        callback?.invoke(formattingDate())
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } finally {
                    lock.unlock()
                }
            }
            resetTimer()
        }

    }

    private fun formattingDate(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val formatter = DateTimeFormatter.ofPattern(customTimePattern)
            val localTime = LocalTime.MIDNIGHT.plus(counter.get().toLong(), ChronoUnit.SECONDS)
            return localTime.format(formatter)
        }
        return END_TIME
    }

    private inner class BackgroundThread : HandlerThread(BACKGROUND_THREAD) {
        private lateinit var handler: Handler

        init {
            start()
        }

        override fun onLooperPrepared() {
            super.onLooperPrepared()
            handler = Handler(looper)
        }

        fun executeTask(runnable: Runnable) {
            handler.post(runnable)
        }
    }

    companion object {
        private const val DEFAULT_TIMER_VALUE = 60
        private const val TICK_INTERVAL = 1L
        private const val BACKGROUND_THREAD = "BACKGROUND_THREAD"
        private const val DEFAULT_TIME_PATTERN = "HH:mm:ss"
        const val END_TIME = "00:00:00"
    }
}