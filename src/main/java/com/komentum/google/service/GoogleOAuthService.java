package com.komentum.google.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class GoogleOAuthService {
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public GoogleOAuthService(GoogleIdTokenVerifier googleIdTokenVerifier) {
        this.googleIdTokenVerifier = googleIdTokenVerifier;
    }

    public GoogleIdToken.Payload verifyToken(String idToken)  {
        GoogleIdToken googleIdToken = null;
        try {
            googleIdToken = googleIdTokenVerifier.verify(idToken);
        }
        catch(GeneralSecurityException | IOException e){
            return null;

        }
        if(googleIdToken == null){
            return null;
        }
        return googleIdToken.getPayload();
    }
}
