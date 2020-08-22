package com.kenvix.complexbot.feature.inspector

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheStats
import com.google.common.cache.LoadingCache
import com.kenvix.moecraftbot.ng.Defines
import com.kenvix.moecraftbot.ng.lib.Cached
import com.kenvix.moecraftbot.ng.lib.CachedClasses
import com.kenvix.moecraftbot.ng.lib.cacheLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import org.litote.kmongo.newId
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


object InspectorStatisticUtils : Cached {
    private val statCache: LoadingCache<Long, InspectorStatistic> = CacheBuilder.newBuilder().apply {
        recordStats()
        expireAfterWrite(SaveFrequentSeconds, TimeUnit.SECONDS)
        removalListener<Long, InspectorStatistic> { runBlocking { saveStat(it.value) } }
    }.build(cacheLoader { groupId ->
        runBlocking {
            groupMongoCollection.findOne(InspectorStatistic::groupId eq groupId)
                ?: createStat(groupId)
        }
    })

    private val groupMongoCollection: CoroutineCollection<InspectorStatistic>
            = Defines.mongoDatabase.getCollection("inspectorStatistics")

    val todayKey: Int
        get() = Calendar.getInstance().run {
            get(Calendar.YEAR) * 1_0000 + get(Calendar.MONTH) * 100 + get(Calendar.DAY_OF_MONTH)
        }

    init {
        CachedClasses.add(this)
    }

    const val MaxRecordedStatDays: Int = 7
    const val SaveFrequentSeconds: Long = 100_000L

    suspend fun getStat(groupId: Long): InspectorStatistic = withContext(Dispatchers.IO) {
        statCache[groupId]
    }

    suspend fun putMemberJoinStat(event: MemberJoinRequestEvent) {
        getStat(event.group.id).run {
            val stat = JoinStatistic(
                qq = event.fromId,
                name = event.fromNick,
                requestedAt = Date(),
                note = event.message,
                status = JoinStatus.Requested.statusId,
                eventId = event.eventId
            )

            joins[stat.qq] = stat
        }
    }

    suspend fun updateMemberJoinStatus(qq: Long, group: Long, status: Int) {
        getStat(group).also { statIdMapValue ->
            statIdMapValue.joins[qq]?.apply {
                this.status = status
            }
        }
    }

    suspend fun addMemberCountStat(member: Member, isIllegal: Boolean = false) {
        getStat(member.group.id).run {
            val today = todayKey

            this.stats[member.id].also { userStatistic ->
                if (userStatistic == null) {
                    this.stats[member.id] = UserStatistic(
                        member.id,
                        member.nick,
                        member.nameCard,
                        if (isIllegal) 1 else 0
                    )
                } else {
                    userStatistic.countTotal++
                    if (isIllegal)
                        userStatistic.countIllegal++

                    if (userStatistic.name != member.nick)
                        userStatistic.name = member.nick

                    if (userStatistic.cardName != member.nameCard)
                        userStatistic.cardName = member.nameCard

                    userStatistic.counts[today].also {
                        if (it == null) {
                            userStatistic.counts[today] = 1

                            if (userStatistic.counts.size > MaxRecordedStatDays) {
                                userStatistic.counts
                                    .minByOrNull { entry -> entry.key }
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
        }
    }

    private suspend fun createStat(groupId: Long): InspectorStatistic = withContext(Dispatchers.IO) {
        InspectorStatistic(groupId = groupId).also { groupMongoCollection.insertOne(it) }
    }

    suspend fun getAllStats(): List<InspectorStatistic> {
        return groupMongoCollection.find().toList()
    }

    suspend fun saveStat(groupId: Long) = withContext(Dispatchers.IO) {
        val stat = getStat(groupId)
        stat.updatedAt = Date()
        groupMongoCollection.updateOneById(stat._id, stat)
    }

    suspend fun saveStat(stat: InspectorStatistic) = withContext(Dispatchers.IO) {
        stat.updatedAt = Date()
        groupMongoCollection.updateOneById(stat._id, stat)
    }

    override fun invalidateAll() {
        statCache.invalidateAll()
    }

    override fun cleanUpAll() {
        statCache.cleanUp()
    }

    override fun getStats(): List<CacheStats> {
        return listOf(statCache.stats())
    }
}