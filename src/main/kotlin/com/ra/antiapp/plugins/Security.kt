package com.ra.antiapp.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.concurrent.TimeUnit

fun Application.configureSecurity() {

    // This is the value you will get from the Google Cloud Console when you set up
    // OAuth 2.0 for your Android/iOS app. It's unique to your app.
    // We'll use a placeholder for now.
    val googleClientId = "YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com"

    // Google's public keys are used to verify the signature of the ID tokens.
    val jwkProvider = JwkProviderBuilder("https://www.googleapis.com/oauth2/v3/certs")
        .cached(10, 24, TimeUnit.HOURS)
        .build()

    // Install the Authentication plugin.
    install(Authentication) {
        // Configure a JWT authenticator named "google-jwt".
        jwt("google-jwt") {
            // The "realm" is a string sent back to the client if they don't provide a token.
            realm = "Anti-App"

            // This block configures the validation logic.
            verifier(jwkProvider) {
                // Tell Ktor which issuer is considered valid. For Google, it's this URL.
                withIssuer("https://accounts.google.com")
                // And which audience is valid (your app's client ID).
                withAudience(googleClientId)
            }

            // This block defines how to convert a valid token into a "Principal" object.
            // A Principal is an object that represents the authenticated user inside your app.
            validate { credential ->
                // The "sub" (subject) claim in a Google token is the user's unique ID.
                // We check if it exists and, if so, create our own UserPrincipal.
                val userId = credential.payload.subject
                if (userId != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}