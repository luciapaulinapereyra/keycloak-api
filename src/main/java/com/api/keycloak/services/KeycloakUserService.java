package com.api.keycloak.services;
import com.api.keycloak.dto.UserDTO;
import com.api.keycloak.dto.UserRequestDTO;
import com.api.keycloak.security.KeycloakProvider;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

@Service
public class KeycloakUserService {

    @Value("${keycloak.realm}")
    public String realm;

    @Autowired
    private KeycloakProvider keycloakProvider;

    @Value("${keycloak.resource}")
    public String clientID;


    public UserDTO registerUserInKeycloak(UserRequestDTO request, String role) {
        UsersResource usersResource = keycloakProvider.getInstance().realm(realm).users();

        List<UserRepresentation> existingsUsers = usersResource.search(request.getUsername());

        if (!existingsUsers.isEmpty()) {
            return null;
        }

        try {
            Response response = usersResource.create(setKeycloakUser(request));
            String userId = CreatedResponseUtil.getCreatedId(response);
            setUserRoles(userId, usersResource, role);
            UserDTO res = new UserDTO();
            res.setEmail(request.getEmail());
            res.setName(request.getUsername());
            res.setRole(role);
            res.setKeycloakId(request.getEmail());
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    public UserRepresentation setKeycloakUser(UserRequestDTO request) {
        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setEnabled(true);
        kcUser.setUsername(request.getUsername());
        kcUser.setEmail(request.getEmail());
        kcUser.setEmailVerified(true);
        CredentialRepresentation passwordCredentials = createPasswordCredentials(request.getPassword());
        kcUser.setCredentials(Arrays.asList(passwordCredentials));
        return kcUser;
    }

    public void setUserRoles(String userId, UsersResource usersResource, String role) {
        Keycloak keycloak = keycloakProvider.getInstance();

        RealmResource realmResource = keycloak.realm(realm);

        UserResource userResource = usersResource.get(userId);

        ClientRepresentation appClient = realmResource.clients().findByClientId(clientID).get(0);


        RoleRepresentation userClientRole = realmResource.clients().get(appClient.getId()).roles()
                .get(role).toRepresentation();

        userResource.roles().clientLevel(appClient.getId()).add(Arrays.asList(userClientRole));
    }

    public CredentialRepresentation createPasswordCredentials(String pass) {
        CredentialRepresentation passwordCredentials = new CredentialRepresentation();
        passwordCredentials.setTemporary(false);
        passwordCredentials.setType(CredentialRepresentation.PASSWORD);
        passwordCredentials.setValue(pass);
        return passwordCredentials;
    }

    public AccessTokenResponse login(UserRequestDTO request) {
        Keycloak keycloak = keycloakProvider.newKeycloakBuilderWithPasswordCredentials(request.getUsername(), request.getPassword()).build();
        AccessTokenResponse tokenResponse = keycloak.tokenManager().getAccessToken();
        return tokenResponse;
    }
}
