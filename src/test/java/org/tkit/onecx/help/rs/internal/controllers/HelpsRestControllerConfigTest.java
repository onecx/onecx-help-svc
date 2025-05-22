package org.tkit.onecx.help.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tkit.onecx.help.domain.config.HelpConfig;
import org.tkit.onecx.help.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.help.rs.internal.model.HelpDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.config.SmallRyeConfig;

@QuarkusTest
@TestHTTPEndpoint(HelpsRestController.class)
@WithDBData(value = "data/test-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = "ocx-hp:all")
class HelpsRestControllerConfigTest extends AbstractTest {

    @InjectMock
    HelpConfig helpConfig;

    @Inject
    Config config;

    @BeforeEach
    void beforeEach() {
        var tmp = config.unwrap(SmallRyeConfig.class).getConfigMapping(HelpConfig.class);
        Mockito.when(helpConfig.defaultHelpEnabled()).thenReturn(true);
        Mockito.when(helpConfig.defaultHelpUrl()).thenReturn(tmp.defaultHelpUrl());
        Mockito.when(helpConfig.productItemId()).thenReturn(tmp.productItemId());
    }

    @Test
    void searchByProductNameAndItemIdNoFoundTest() {

        var data = given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .get("does-not-exists/cg")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(HelpDTO.class);

        assertThat(data).isNotNull();
        Assertions.assertEquals("does-not-exists", data.getProductName());
        Assertions.assertEquals("cg", data.getItemId());
        Assertions.assertEquals(helpConfig.defaultHelpUrl(), data.getBaseUrl());
    }

}
