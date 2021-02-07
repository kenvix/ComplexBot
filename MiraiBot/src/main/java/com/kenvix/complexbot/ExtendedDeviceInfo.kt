//--------------------------------------------------
// Class DeviceInfo
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.complexbot

import com.kenvix.moecraftbot.ng.lib.generateUUID
import com.kenvix.moecraftbot.ng.lib.getRandomByteArray
import com.kenvix.moecraftbot.ng.lib.getRandomIntString
import com.kenvix.moecraftbot.ng.lib.md5
import net.mamoe.mirai.utils.DeviceInfo

fun newExtendedDeviceInfo() : DeviceInfo = DeviceInfo(
    display = "Galaxy.38402.160".toByteArray(),
    product = "Galaxy Tab S2 8.0".toByteArray(),
    device = "Galaxy Tab S2 8.0".toByteArray(),
    board = "CMCC".toByteArray(),
    brand = "SAMSUNG".toByteArray(),
    model = "SM-T710".toByteArray(),
    procVersion =
        "Linux version 3.8.31-1dyj5zxe (android-build@xiaomi.com)".toByteArray(),
    fingerprint =
        "SAMSUNG/Galaxy/Tab S2:8/Galaxy.38402.160/1dyj5zx:user/release-keys".toByteArray(),
    bootloader = "unknown".toByteArray(),
    bootId = generateUUID(getRandomByteArray(16).md5()).toByteArray(),
    baseBand = byteArrayOf(),
    version = DeviceInfo.Version(),
    simInfo = "T-Mobile".toByteArray(),
    osType = "android".toByteArray(),
    macAddress = "02:00:00:00:00:00".toByteArray(),
    wifiBSSID = "02:00:00:00:00:00".toByteArray(),
    wifiSSID = "<unknown ssid>".toByteArray(),
    imsiMd5 = getRandomByteArray(16).md5(),
    imei = getRandomIntString(15),
    apn = "wifi".toByteArray()
)