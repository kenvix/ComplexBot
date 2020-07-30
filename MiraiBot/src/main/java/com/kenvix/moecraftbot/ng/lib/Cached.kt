//--------------------------------------------------
// Interface Cached
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib

import com.google.common.cache.CacheStats

interface Cached {
    fun invalidateAll()
    fun cleanUpAll()
    fun getStats(): List<CacheStats>
}