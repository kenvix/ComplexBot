//--------------------------------------------------
// Class GroupOptions
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.complexbot

import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class GroupOptions(
        @BsonId
        val _id: Id<GroupOptions> = newId(),
        val groupId: Long = -1,
        val inspector: InspectorOptions? = null
)

data class InspectorOptions(
        val enabled: Boolean = false,
        val rules: Map<String, String> = emptyMap(),
        val white: List<Long> = emptyList()
)