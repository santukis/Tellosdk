package com.santukis.tellosdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.santukis.tello.DefaultTelloController
import com.santukis.tello.DefaultTelloMonitor
import com.santukis.tello.TelloController
import com.santukis.tello.TelloMonitor
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), TelloMonitor.OnStateChanged {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val controller: TelloController = DefaultTelloController()

        controller.connect(object : TelloController.TelloConnectionListener {
            override fun onTelloConnected() {
                val monitor = DefaultTelloMonitor()
                monitor.startMonitoring(this@MainActivity)
            }

            override fun onErrorConnectingTello(error: String) {
                Log.d("TELLO", "Error connecting tello $error")
            }
        })

        takeoff.setOnClickListener { controller.takeOff() }

        landing.setOnClickListener { controller.land() }
    }

    override fun onBatteryChanged(level: Double) {
        Log.d("TELLO", "Battery level $level%")
    }
}
