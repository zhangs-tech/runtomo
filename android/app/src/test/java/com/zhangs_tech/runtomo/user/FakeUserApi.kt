package com.zhangs_tech.runtomo.user

class FakeUserApi : UserApi {
    var me = Me(displayName = "Kai Tan", friendCode = "00000001", coinBalance = 0, hasTomo = true)
    var error: Exception? = null
    val savedDisplayNames = mutableListOf<String>()

    override suspend fun getMe(): Me {
        error?.let { throw it }
        return me
    }

    override suspend fun updateDisplayName(displayName: String): Me {
        error?.let { throw it }
        savedDisplayNames += displayName
        me = me.copy(displayName = displayName)
        return me
    }
}
