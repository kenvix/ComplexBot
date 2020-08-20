//--------------------------------------------------
// Class DeviceInfo
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.complexbot

import net.mamoe.mirai.utils.SystemDeviceInfo

object ExtendedDeviceInfo : SystemDeviceInfo() {
    override val display: ByteArray = "Galaxy.38402.160".toByteArray()
    override val product: ByteArray = "Galaxy Tab S2 8.0".toByteArray()
    override val device: ByteArray = "Galaxy Tab S2 8.0".toByteArray()
    override val board: ByteArray = "CMCC".toByteArray()
    override val brand: ByteArray = "SAMSUNG".toByteArray()
    override val model: ByteArray = "SM-T710".toByteArray()
    override val procVersion: ByteArray =
        "Linux version 3.8.31-1dyj5zxe (android-build@xiaomi.com)".toByteArray()
    override val fingerprint: ByteArray =
        "SAMSUNG/Galaxy/Tab S2:8/Galaxy.38402.160/1dyj5zx:user/release-keys".toByteArray()
}