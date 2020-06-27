//--------------------------------------------------
// Class DeviceInfo
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.complexbot

import net.mamoe.mirai.utils.SystemDeviceInfo

object ExtendedDeviceInfo : SystemDeviceInfo() {
    override val display: ByteArray = "PIXEL.748698.001".toByteArray()
    override val product: ByteArray = "pixel".toByteArray()
    override val device: ByteArray = "pixel".toByteArray()
    override val board: ByteArray = "google".toByteArray()
    override val brand: ByteArray = "google".toByteArray()
    override val model: ByteArray = "pixel".toByteArray()
}