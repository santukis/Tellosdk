package com.santukis.tello

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean

class DefaultTelloMonitor : TelloMonitor {

    companion object {
        private const val UNDEFINED = -1.0

        private const val PITCH = "pitch"
        private const val ROLL = "roll"
        private const val YAW = "yaw"
        private const val X_SPEED = "vgx"
        private const val Y_SPEED = "vgy"
        private const val Z_SPEED = "vgz"
        private const val LOWEST_TEMP = "templ"
        private const val HIGHEST_TEMP = "temph"
        private const val TOF = "tof"
        private const val HEIGHT = "h"
        private const val BATTERY = "bat"
        private const val BAROMETER = "baro"
        private const val X_ACCELERATION = "agx"
        private const val Y_ACCELERATION = "agy"
        private const val Z_ACCELERATION = "agz"
    }

    private var scanningInterval = 2000L

    private lateinit var socket: DatagramSocket

    private var monitoring: AtomicBoolean = AtomicBoolean(false)

    private val buffer: ByteArray = ByteArray(1024)

    override fun startMonitoring(listener: TelloMonitor.OnStateChanged) {
        try {
            socket = DatagramSocket(8890, InetAddress.getByName("0.0.0.0"))
            monitoring.set(true)

            GlobalScope.launch {
                while (monitoring.get()) {
                    val response = readData()

                    val parameters = readParameters(response)

                    checkParameters(parameters, listener)

                    Log.d("TELLO", parameters.toString())

                    delay(scanningInterval)
                }
            }
        } catch (exception: Exception) {
            Log.d("TELLO", exception.localizedMessage)
        }
    }

    override fun stopMonitoring() {
        monitoring.set(false)
    }

    override fun setScanningInterval(interval: Long) {
        this.scanningInterval = interval
    }

    private fun readData(): String {
        val receivePacket = DatagramPacket(buffer, buffer.size)
        socket.receive(receivePacket)

        return String(receivePacket.data)
    }

    private fun readParameters(response: String): Map<String, Double> {
        return try {
            response.subSequence(0, response.indexOf("\n\r"))
                .split(";").associate {
                    val (left, right) = it.split(":")
                    left to right.toDouble()
                }
        } catch (exception: Exception) {
            Log.e("TELLO", exception.localizedMessage)

            emptyMap()
        }
    }

    private fun checkParameters(parameters: Map<String, Double>, listener: TelloMonitor.OnStateChanged) {
        execute(Dispatchers.Main) {
            listener.onAttitudeChanged(
                pitch = parameters[PITCH] ?: UNDEFINED,
                roll = parameters[ROLL] ?: UNDEFINED,
                yaw = parameters[YAW] ?: UNDEFINED
            )

            listener.onSpeedChanged(
                x = parameters[X_SPEED] ?: UNDEFINED,
                y = parameters[Y_SPEED] ?: UNDEFINED,
                z = parameters[Z_SPEED] ?: UNDEFINED
            )

            listener.onTemperatureChanged(
                lowest = parameters[LOWEST_TEMP] ?: UNDEFINED,
                highest = parameters[HIGHEST_TEMP] ?: UNDEFINED
            )

            listener.onTofChanged(distance = parameters[TOF] ?: UNDEFINED)

            listener.onHeightChanged(height = parameters[HEIGHT] ?: UNDEFINED)

            listener.onBatteryChanged(level = parameters[BATTERY] ?: UNDEFINED)

            listener.onBarometerChanged(distance = parameters[BAROMETER] ?: UNDEFINED)

            listener.onAccelerationChanged(
                x = parameters[X_ACCELERATION] ?: UNDEFINED,
                y = parameters[Y_ACCELERATION] ?: UNDEFINED,
                z = parameters[Z_ACCELERATION] ?: UNDEFINED
            )
        }
    }
}