package de.adorsys.ledgers.keycloak.client.impl;

import de.adorsys.ledgers.keycloak.client.api.KeycloakDataService;
import de.adorsys.ledgers.keycloak.client.mapper.KeycloakDataMapper;
import de.adorsys.ledgers.keycloak.client.mapper.KeycloakDataMapperImpl;
import de.adorsys.ledgers.keycloak.client.model.KeycloakClient;
import de.adorsys.ledgers.keycloak.client.model.KeycloakUser;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import pro.javatar.commons.reader.JsonReader;
import pro.javatar.commons.reader.ResourceReader;
import utils.KeycloakContainerTest;
import utils.TestConfiguration;

import javax.ws.rs.ClientErrorException;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@ContextConfiguration(classes = {TestConfiguration.class, KeycloakDataMapperImpl.class},
        initializers = KeycloakDataServiceImplIT.Initializer.class)
public class KeycloakDataServiceImplIT extends KeycloakContainerTest {

    private static final String REALM = "test-realm";
    private static final String CLIENT_ID = "ledgers-client";
    private static final String CLIENT_SECRET = "279976b2-1794-437f-9467-8ad8401f1c51";
    private static final String USERNAME = "babyuk";

    @Autowired
    private KeycloakDataMapper keycloakDataMapper;

    private KeycloakDataService keycloakDataService;
    private ResourceReader jsonReader = JsonReader.getInstance();

    @Before
    public void setUp() {
        keycloakDataService = new KeycloakDataServiceImpl(getKeycloakClient(), keycloakDataMapper);
    }

    @AfterClass
    public static void afterClass() {
        keycloakContainer.stop();
    }

    @Test
    public void createRealm() {
        keycloakDataService.createRealm(REALM);
        //error when realm with same name has already existed
        assertThrows(ClientErrorException.class, () -> keycloakDataService.createRealm(REALM));
    }

    @Test
    public void createClient() {
        keycloakDataService.createClient(new KeycloakClient(REALM, CLIENT_ID, CLIENT_SECRET));
        assertTrue(keycloakDataService.clientExists(REALM, CLIENT_ID));
    }

    @Test
    public void crudUser() throws IOException {
        //create user in keycloak
        KeycloakUser keycloakUser = jsonReader.getObjectFromFile("json/keycloak/create-user.json", KeycloakUser.class);
        keycloakDataService.createUser(REALM, keycloakUser);
        assertTrue(keycloakDataService.userExists(REALM, USERNAME));
        assertEquals("Eugen", keycloakUser.getFirstName());

        //update first name in keycloak
        KeycloakUser keycloakUserToUpdate = jsonReader.getObjectFromFile("json/keycloak/update-user.json", KeycloakUser.class);
        keycloakDataService.updateUser(REALM, keycloakUserToUpdate);

        //get user by login and check first name
        Optional<KeycloakUser> userOptional = keycloakDataService.getUser(REALM, USERNAME);
        assertTrue(userOptional.isPresent());
        assertEquals("Semen", userOptional.get().getFirstName());

        //delete user from keycloak
        keycloakDataService.deleteUser(REALM, USERNAME);
        assertFalse(keycloakDataService.userExists(REALM, USERNAME));
    }
}