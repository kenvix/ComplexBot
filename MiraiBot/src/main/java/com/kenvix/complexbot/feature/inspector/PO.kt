package com.kenvix.complexbot.feature.inspector

import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.*
import kotlin.collections.HashMap

enum class JoinStatus(val statusId: Int) {
    Requested(0),
    Accepted(1),
    Denied(2),
    Left(3)
}

data class InspectorStatistic(
    @BsonId
    val _id: Id<InspectorStatistic> = newId(),
    val groupId: Long = -1,
    var updatedAt: Date = Date(),
    val createdAt: Date = Date(),
    val stats: MutableMap<Long, UserStatistic> = HashMap(),
    val joins: MutableMap<Long, JoinStatistic> = HashMap()
)

data class UserStatistic(
    val qq: Long,
    var name: String,
    var cardName: String? = null,
    var countTotal: Long = 0,
    var countIllegal: Long = 0,
    val counts: MutableMap<Int, Long> = HashMap(),
    val createdAt: Date = Date()
) {
    val countLegal: Long
        get() = countTotal - countIllegal
}

data class JoinStatistic(
    val qq: Long,
    val eventId: Long,
    val name: String,
    val requestedAt: Date = Date(),
    var handledAt: Date? = null,
    var status: Int = 0,
    val note: String = ""
)