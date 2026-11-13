package com.zhangs_tech.runtomo.auth

import android.content.Context
import android.content.MutableContextWrapper
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

/**
 * Gets a Google ID token through Credential Manager.
 *
 * @param serverClientId the web OAuth client ID. The server accepts only ID tokens for this audience.
 */
class GoogleIdentityProvider(private val serverClientId: String) : IdentityProvider {

    override suspend fun requestIdToken(activityContext: Context): IdTokenResult {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(serverClientId).build())
            .build()

        return try {
            // Google recommends a MutableContextWrapper around the foreground Activity for getCredential.
            val context = MutableContextWrapper(activityContext)
            val credential = CredentialManager.create(context).getCredential(context, request).credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                IdTokenResult.Success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } else {
                IdTokenResult.Failed(IllegalStateException("Unexpected credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            IdTokenResult.Cancelled
        } catch (e: NoCredentialException) {
            IdTokenResult.NoAccount
        } catch (e: GetCredentialException) {
            IdTokenResult.Failed(e)
        } catch (e: GoogleIdTokenParsingException) {
            IdTokenResult.Failed(e)
        }
    }
}
