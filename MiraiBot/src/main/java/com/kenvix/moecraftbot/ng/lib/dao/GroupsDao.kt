package com.kenvix.moecraftbot.ng.lib.dao

import com.kenvix.moecraftbot.ng.jooqConfiguration
import com.kenvix.moecraftbot.ng.orm.tables.daos.GroupsDao

object GroupsDao : GroupsDao(jooqConfiguration)
