package com.api.keycloak.security;


import org.keycloak.OAuth2Constants;
import org.keycloak.TokenVerifier;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.UserRepresentation;
;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakProvider {
    @Value("${keycloak.auth-server-url}")
    public String serverURL;
    @Value("${keycloak.resource}")
    public String clientID;
    @Value("${keycloak.realm}")
    public String realm;
    @Value("${keycloak.credentials.secret}")
    public String clientSecret;


    public KeycloakProvider() {
    }

    public Keycloak getInstance() {
        return KeycloakBuilder.builder()
                .realm(realm)
                .serverUrl(serverURL)
                .clientId(clientID)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

    public KeycloakBuilder newKeycloakBuilderWithPasswordCredentials(String username, String password) {
        return KeycloakBuilder.builder() //
                .realm(realm) //
                .serverUrl(serverURL)//
                .clientId(clientID) //
                .clientSecret(clientSecret) //
                .username(username) //
                .password(password);
    }

    public Keycloak newKeycloakBuilderWithClientCredentials(String clientId, String clientSecret) {
        // Construye una instancia de Keycloak con las credenciales de cliente
        return KeycloakBuilder.builder()
                .serverUrl(serverURL)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

    public Keycloak newKeycloakBuilderWithNewRealm(String newRealm, String newClientSecret, String newClientId) {
        // Construye una instancia de Keycloak con las credenciales de cliente
        return KeycloakBuilder.builder()
                .serverUrl(serverURL)
                .realm(newRealm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(newClientId)
                .clientSecret(newClientSecret)
                .build();
    }

    public AccessToken getTokenVerified(String token) throws VerificationException {
        AccessToken accessToken = TokenVerifier.create(token, AccessToken.class).getToken();
        return accessToken;
    }

    public UserRepresentation getUserRepresentation(String userId) {
        UserRepresentation userRepresentation = getInstance().realm(realm).users().get(userId).toRepresentation();
        return userRepresentation;
    }


}