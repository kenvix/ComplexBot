@file:JvmName("DatabaseConnection")
package com.kenvix.moecraftbot.ng

import org.apache.commons.dbcp2.BasicDataSource
import org.jooq.DSLContext
import java.sql.Connection

val jooqConfiguration
    get() = Defines.jooqConfiguration

val dslContext: DSLContext
    get() = Defines.dslContext

val dataSource: BasicDataSource
    get() = Defines.dataSource

val sqlConnection: Connection
    get() = dataSource.connection