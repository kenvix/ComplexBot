package com.kenvix.complexbot.feature.inspector

import com.kenvix.android.utils.Coroutines
import com.kenvix.moecraftbot.ng.Defines
import com.kenvix.utils.exception.NotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Member
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import org.litote.kmongo.newId
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule


object InspectorStatisticUtils {
    private val groupIdMap: MutableMap<Long, StatIdMapValue> = HashMap()
    private val statCacheMap: MutableMap<Long, InspectorStatistic> = HashMap()
    private val groupMongoCollection: CoroutineCollection<InspectorStatistic>
            = Defines.mongoDatabase.getCollection("inspectorStatistics")
    private val saveTimer = Timer()
    private var calendar = Calendar.getInstance()
    val todayKey: Int
        get() = calendar.get(Calendar.YEAR) * 1_0000 +
                calendar.get(Calendar.MONTH) * 100 +
                calendar.get(Calendar.DAY_OF_MONTH)

    init {
        scheduleSaveTimerTask()
    }

    const val MaxRecordedStatDays: Int = 7
    const val SaveFrequentSeconds: Long = 100_000L

    private fun scheduleSaveTimerTask() {
        saveTimer.schedule(SaveFrequentSeconds) {
            // Run save task on timer thread to prevent concurrent save caused by timer
            runBlocking { saveAllStat() }
            scheduleSaveTimerTask()
        }
    }

    suspend fun saveAllStat() {
        groupIdMap.forEach { (groupId, v) ->
            if (v.isChanged) {
                saveStat(groupId)
            }
        }
    }

    suspend fun getStat(groupId: Long): InspectorStatistic = withContext(Dispatchers.IO) {
        statCacheMap[groupId].let {
            if (it != null) {
                it
            } else {
                val op = groupMongoCollection.findOne(InspectorStatistic::groupId eq groupId)
                    ?: createStat(groupId)

                statCacheMap[groupId] = op
                groupIdMap[groupId] = StatIdMapValue(op._id)
                op
            }
        }
    }

    suspend fun addMemberCountStat(member: Member, isIllegal: Boolean = false) {
        getStat(member.group.id).run {
            groupIdMap[member.group.id]!!.also { statIdMapValue ->
                val today = todayKey
                statIdMapValue.mutex.withLock {
                    this.stats[member.id].also { userStatistic ->
                        if (userStatistic == null) {
                            this.stats[member.id] = UserStatistic(member.id, member.nick, 1, if (isIllegal) 1 else 0)
                        } else {
                            userStatistic.countTotal++
                            if (isIllegal)
                                userStatistic.countIllegal++

                            if (userStatistic.name != member.nick)
                                userStatistic.name = member.nick

                            userStatistic.counts[today].also {
                                if (it == null) {
                                    userStatistic.counts[today] = 1

                                    if (userStatistic.counts.size > MaxRecordedStatDays) {
                                        userStatistic.counts
                                            .minBy { entry -> entry.key }
                                            .also { entry ->
                                                if (entry != null)
                                                    userStatistic.counts.remove(entry.key)
                                            }
                                    }
                                } else {
                                    userStatistic.counts[today] = it + 1
                                }
                            }
                        }
                    }

                    if (!statIdMapValue.isChanged)
                        statIdMapValue.isChanged = true
                }
            }
        }
    }

    private suspend fun createStat(groupId: Long): InspectorStatistic = withContext(Dispatchers.IO) {
        InspectorStatistic(groupId = groupId).also { groupMongoCollection.insertOne(it) }
    }

    suspend fun getAllStats(): List<InspectorStatistic> {
        return groupMongoCollection.find().toList()
    }

    suspend fun saveStat(groupId: Long) = withContext(Dispatchers.IO) {
        val objId = groupIdMap[groupId] ?: throw NotFoundException("No such group in cache")
        objId.isChanged = false
        objId.mutex.withLock {
            val stat = getStat(groupId)
            stat.updatedAt = Date()
            groupMongoCollection.updateOneById(objId._id, stat)
        }
    }
}

data class StatIdMapValue(
    val _id: Id<InspectorStatistic>,
    var isChanged: Boolean = false,
    val mutex: Mutex = Mutex()
)

data class InspectorStatistic(
    @BsonId
    val _id: Id<InspectorStatistic> = newId(),
    val groupId: Long = -1,
    var updatedAt: Date = Date(),
    val createdAt: Date = Date(),
    val stats: MutableMap<Long, UserStatistic> = HashMap()
)

data class UserStatistic(
    val qq: Long,
    var name: String,
    var countTotal: Long = 0,
    var countIllegal: Long = 0,
    val counts: MutableMap<Int, Long> = HashMap(),
    val createdAt: Date = Date()
) {
    val countLegal: Long
        get() = countTotal - countIllegal
}