package com.santukis.tello

interface TelloController {

    interface TelloConnectionListener{
        fun onTelloConnected()
        fun onErrorConnectingTello(error: String)
    }

    fun connect(listener: TelloConnectionListener)

    fun disconnect()

    fun takeOff();

    fun land();

    fun emergency()

    fun streamOn()

    fun streamOff()

    fun moveLeft(x: Int)

    fun moveRight(x: Int)

    fun moveForward(y: Int)

    fun moveBack(y: Int)

    fun moveUp(z: Int)

    fun moveDown(z: Int)

    fun rotateClockwise(degrees: Int)

    fun rotateCounterClockwise(degrees: Int)

    fun flip(direction: FlipDirection)

    fun go(x: Int, y: Int, z: Int, speed: Int)

    fun curve(x1: Int, x2: Int, y1: Int, y2: Int, z1: Int, z2: Int, speed: Int)

    fun setSpeed(speed: Int)

    fun setWifiSsidPass(ssid: String, pass: String)

    fun sendRc(leftRight: Int, forwardBack: Int, upDown: Int, yaw: Int)

    fun read(info: Info)
}