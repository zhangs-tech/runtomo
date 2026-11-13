package com.zhangs_tech.runtomo.network

import javax.inject.Qualifier

/** The [io.ktor.client.HttpClient] that sends the Player's access token. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedClient
