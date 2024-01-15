package com.example.mytimer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mytimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val timer = MyTimer()
    private val timerCallback: (String) -> Unit = { counter->
        if (counter == MyTimer.END_TIME) showToast()
        binding.tvTimer.text = counter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also{ setContentView(it.root) }

        with(binding){
            tvTimer.text = timer.getDefaultValue()

            btnStartTimer.setOnClickListener {
                timer.startTimer(timerCallback)
            }

            btnPauseTimer.setOnClickListener {
                timer.pauseTimer()
            }
            btnResetTimer.setOnClickListener {
                timer.resetTimer()
            }
        }
    }

    override fun onDestroy() {
        timer.quitTimer()
        super.onDestroy()
    }

    private fun showToast() {
        Toast.makeText(this, getString(R.string.finished_timer), Toast.LENGTH_SHORT).show()
    }

}