package com.komentum.google.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.komentum.auth.JwtUtils;
import com.komentum.user.domain.User;
import com.komentum.user.dto.UserAuthResponse;
import com.komentum.user.repository.UserRepository;
import com.komentum.user.service.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.komentum.google.dto.GoogleUserInfo;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class GoogleOAuthService {
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final JwtUtils jwtUtils;
    private final TransactionTemplate transactionTemplate;

    public GoogleOAuthService( GoogleIdTokenVerifier googleIdTokenVerifier,
                               UserRepository userRepository,
                               TokenService tokenService,
                               JwtUtils jwtUtils,
                               TransactionTemplate transactionTemplate
    ) {
        this.googleIdTokenVerifier = googleIdTokenVerifier;
        this.tokenService = tokenService;
        this.jwtUtils =  jwtUtils;
        this.userRepository = userRepository;
        this.transactionTemplate = transactionTemplate;
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

    public UserAuthResponse processGoogleAuth(String idToken){

        GoogleIdToken.Payload verifiedToken = verifyToken(idToken);
        if (verifiedToken == null){
            throw  new RuntimeException("Invalid Google Token");
        }
        GoogleUserInfo googleUserInfo = GoogleUserInfo.from(verifiedToken);

        User user = userRepository.findById(googleUserInfo.getEmail()).orElse(null);
        if (user == null){
            user = userRepository.save(googleUserInfo.toEntity());
        }

        String accessToken = jwtUtils.generateAccessToken(user.getUserEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getUserEmail());

        if (!tokenService.saveAccessAndRefreshToken(user.getUserEmail(), accessToken,refreshToken)){
            throw new RuntimeException("failed to save access and refresh token");
        }

        return new UserAuthResponse(accessToken, refreshToken) ;
    }

}
