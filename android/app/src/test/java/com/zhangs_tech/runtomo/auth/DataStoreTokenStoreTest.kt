package com.zhangs_tech.runtomo.auth

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreTokenStoreTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val tokenStore = DataStoreTokenStore(
        PreferenceDataStoreFactory.create(scope = testScope.backgroundScope) {
            temporaryFolder.newFile("tokens.preferences_pb").also { it.delete() }
        },
    )

    @Test
    fun read_whenEmpty_returnsNull() = testScope.runTest {
        assertNull(tokenStore.read())
    }

    @Test
    fun save_thenRead_returnsTokens() = testScope.runTest {
        tokenStore.save(AuthTokens("a", "r"))

        assertEquals(AuthTokens("a", "r"), tokenStore.read())
    }

    @Test
    fun clear_removesTokens() = testScope.runTest {
        tokenStore.save(AuthTokens("a", "r"))

        tokenStore.clear()

        assertNull(tokenStore.read())
    }
}
