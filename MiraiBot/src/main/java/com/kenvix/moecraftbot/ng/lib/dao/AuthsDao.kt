//--------------------------------------------------
// Class AuthModel
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.dao

import com.kenvix.moecraftbot.ng.dslContext
import com.kenvix.moecraftbot.ng.jooqConfiguration
import com.kenvix.moecraftbot.ng.lib.bot.BotUser
import com.kenvix.moecraftbot.ng.lib.middleware.auth.AuthenticatedUserLevel
import com.kenvix.moecraftbot.ng.lib.middleware.auth.BotAuthData
import com.kenvix.moecraftbot.ng.lib.middleware.auth.BotAuthSession
import com.kenvix.moecraftbot.ng.orm.tables.Auths.AUTHS
import com.kenvix.moecraftbot.ng.orm.tables.daos.AuthsDao
import com.kenvix.moecraftbot.ng.orm.tables.pojos.Auths
import java.sql.Timestamp

object AuthsDao : AuthsDao(jooqConfiguration) {

    fun countAll(): Long = count()

    fun updateSiteInfoById(id: Int, authData: BotAuthData) = dslContext.update(AUTHS)
        .set(AUTHS.SITE_USER_ID, authData.uid)
        .set(AUTHS.SITE_USER_NAME, authData.name ?: "")
        .set(AUTHS.SITE_USER_TOKEN, authData.token ?: "")
        .where(AUTHS.ID.eq(id))
        .execute()

    fun updateTgNameById(id: Int, name: String) = dslContext.update(AUTHS)
        .set(AUTHS.TG_USER_NAME, name)
        .where(AUTHS.ID.eq(id))
        .execute()

    fun updateTgDescriptionById(id: Int, description: String) = dslContext.update(AUTHS)
        .set(AUTHS.TG_USER_DESCRIPTION, description)
        .where(AUTHS.ID.eq(id))
        .execute()


    fun addAuth(authSession: BotAuthSession, user: BotUser, time: Long = System.currentTimeMillis()) {
        val authObject = Auths()
        authObject.tgUserId = authSession.userId.toInt()
        authObject.tgUserName = user.name
        authObject.tgUserDescription = user.description
        authObject.siteUserId = authSession.data.uid ?: throw IllegalArgumentException()
        authObject.siteUserName = authSession.data.name ?: ""
        authObject.siteUserToken =  authSession.data.token ?: ""
        authObject.level = AuthenticatedUserLevel.USER.levelCode
        authObject.authedAt = Timestamp(time)

        insert(authObject)
    }
}