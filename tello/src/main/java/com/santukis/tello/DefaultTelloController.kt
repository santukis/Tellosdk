package com.santukis.tello

import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class DefaultTelloController : TelloController {

    companion object {
        val distanceRange = 20..500
        val rotationRange = 1..3600
        val speedRange = 1..100
        val rcRange = -100..100

        private const val START = "command"
        private const val TAKE_OFF = "takeoff"
        private const val LAND = "land"
        private const val EMERGENCY = "emergency"
        private const val STREAM_ON = "streamon"
        private const val STREAM_OFF = "streamoff"
        private const val TO_LEFT = "left"
        private const val TO_RIGHT = "right"
        private const val FORWARD = "forward"
        private const val BACKWARD = "back"
        private const val UPWARD = "up"
        private const val DOWNWARD = "down"
        private const val ROTATE_CLOCKWISE = "cw"
        private const val ROTATE_COUNTER_CLOCKWISE = "ccw"
        private const val FLIP = "flip"
        private const val GO = "go"
        private const val CURVE = "curve"
        private const val SPEED = "speed"
        private const val WIFI = "wifi"
        private const val RC = "rc"

        private const val OK = "ok"
        private const val ERROR = "error"
    }

    private lateinit var socket: DatagramSocket

    private var isImperial: AtomicBoolean = AtomicBoolean(false)
    private var isInitialized: AtomicBoolean = AtomicBoolean(false)

    override fun connect(listener: TelloController.TelloConnectionListener) {
        execute {
            try {
                socket = DatagramSocket(8889)
                socket.connect(InetAddress.getByName("192.168.10.1"), 8889)
                initialize()

                listener.onTelloConnected()

            } catch (exception: Exception) {
                listener.onErrorConnectingTello("Unable to connect Tello")
            }
        }
    }

    private fun initialize() {
        execute {
            val response = sendCommand(START)

            when {
                response.startsWith(OK) -> isInitialized.set(true)
                response.startsWith(ERROR) -> isInitialized.set(false)
            }
        }
    }

    override fun disconnect() = socket.close()

    override fun takeOff() {
        execute {
            if (!isInitialized.get())
                initialize()

            sendCommand(TAKE_OFF)
        }
    }

    override fun land() {
        execute { sendCommand(LAND) }
    }

    override fun emergency() {
        execute { sendCommand(EMERGENCY) }
    }

    override fun streamOn() {
        execute { sendCommand(STREAM_ON) }
    }

    override fun streamOff() {
        execute { sendCommand(STREAM_OFF) }
    }

    override fun moveLeft(x: Int) {
        execute { move(TO_LEFT, x) }
    }

    override fun moveRight(x: Int) {
        execute { move(TO_RIGHT, x) }
    }

    override fun moveForward(y: Int) {
        execute { move(FORWARD, y) }
    }

    override fun moveBack(y: Int) {
        execute { move(BACKWARD, y) }
    }

    override fun moveUp(z: Int) {
        execute { move(UPWARD, z) }
    }

    override fun moveDown(z: Int) {
        execute { move(DOWNWARD, z) }
    }

    override fun rotateClockwise(degrees: Int) {
        execute { rotate(ROTATE_CLOCKWISE, degrees) }
    }

    override fun rotateCounterClockwise(degrees: Int) {
        execute { rotate(ROTATE_COUNTER_CLOCKWISE, degrees) }
    }

    override fun flip(direction: FlipDirection) {
        execute { sendCommand("$FLIP ${direction.direction}") }
    }

    override fun go(x: Int, y: Int, z: Int, speed: Int) {
        execute {
            if (arrayListOf(x, y, z).isValidDistance())
                sendCommand("$GO $x $y $z $speed")
        }
    }

    override fun curve(x1: Int, x2: Int, y1: Int, y2: Int, z1: Int, z2: Int, speed: Int) {
        execute {
            if (x1.isValidDistance() &&
                x2.isValidDistance() &&
                y1.isValidDistance() &&
                y2.isValidDistance() &&
                z1.isValidDistance() &&
                z2.isValidDistance() &&
                speed.isValidSpeed()) {

                sendCommand("$CURVE $x1 $y1 $z1 $x2 $y2 $z2 $speed")
            }
        }
    }

    override fun setSpeed(speed: Int) {
        execute {
            if (speed.isValidSpeed())
                sendCommand("$SPEED $speed")
        }
    }


    override fun setWifiSsidPass(ssid: String, pass: String) {
        execute {
            sendCommand("$WIFI $ssid $pass")
        }
    }

    override fun sendRc(leftRight: Int, forwardBack: Int, upDown: Int, yaw: Int) {
        execute {
            if (arrayListOf(leftRight, forwardBack, upDown).isValidRc()) {
                sendCommand("$RC $leftRight $forwardBack $upDown $yaw")
            }
        }
    }

    override fun read(info: Info) {
        execute {
            sendCommand(info.type)
        }
    }

    private fun move(command: String, distance: Int) {
        if (distance.isValidDistance())
            sendCommand("$command $distance")
    }

    private fun rotate(command: String, degrees: Int) {
        if (degrees.isValidRotation())
            sendCommand("$command $degrees")
    }

    private fun sendCommand(command: String): String {
        try {
            if (command.isEmpty()) return "No command"
            if (!socket.isConnected) return "No connected"

            val sendData = command.toByteArray()
            val sendPacket = DatagramPacket(sendData, sendData.size, socket.inetAddress, socket.port)
            socket.send(sendPacket)

            val receiveData = ByteArray(256)
            val receivePacket = DatagramPacket(receiveData, receiveData.size)
            socket.receive(receivePacket)

            val response = String(receivePacket.data)
            Log.d("TELLO", "$command: $response")

            return response

        }  catch (exception: Exception) {
            Log.d("TELLO", exception.localizedMessage)
            return "Error"
        }
    }

    private fun Int.toMetric() = if (!isImperial.get()) this else Math.round((this * 2.54).toFloat())
    private fun Int.isValidDistance() = this.toMetric() in distanceRange
    private fun Int.isValidRotation() = this in rotationRange
    private fun Int.isValidSpeed() = this in speedRange
    private fun Int.isValidRc() = this in rcRange
    private fun ArrayList<Int>.isValidDistance() = this.all { it.isValidDistance() }
    private fun ArrayList<Int>.isValidRc() = this.all { it.isValidRc() }
}

fun execute(context: CoroutineContext = EmptyCoroutineContext, task: () -> Unit) {
    GlobalScope.launch(context) {
        task()
    }
}

enum class Info(val type: String) {
    SPEED("speed?"),
    BATTERY("battery?"),
    TIME("time?"),
    HEIGHT("height?"),
    TEMP("temp?"),
    ATTITUDE("attitude?"),
    BARO("baro?"),
    ACCELERATION("acceleration?"),
    TOF("tof?"),
    WIFI("wifi?")
}

enum class FlipDirection(val direction: String) {
    LEFT("l"),
    RIGHT("r"),
    FORWARD("f"),
    BACKWARD("b"),
    BACK_LEFT("bl"),
    BACK_RIGHT("rb"),
    FRONT_LEFT("fl"),
    FRONT_RIGHT("fr")
}