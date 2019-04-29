package com.santukis.tello

interface TelloMonitor {

    interface OnStateChanged {
        fun onAttitudeChanged(pitch: Double, roll: Double, yaw: Double) {}
        fun onSpeedChanged(x: Double, y: Double, z: Double) {}
        fun onTemperatureChanged(lowest: Double, highest: Double) {}
        fun onTofChanged(distance: Double) {}
        fun onHeightChanged(height: Double) {}
        fun onBarometerChanged(distance: Double) {}
        fun onBatteryChanged(level: Double) {}
        fun onAccelerationChanged(x: Double, y: Double, z: Double) {}
    }

    fun startMonitoring(listener: OnStateChanged)

    fun stopMonitoring()

    fun setScanningInterval(interval: Long)
}