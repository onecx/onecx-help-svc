package org.tkit.onecx.help.rs.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.help.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.help.rs.v1.model.HelpDTOV1;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(HelpsV1RestController.class)
@WithDBData(value = "data/test-v1.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class HelpsV1RestControllerTest extends AbstractTest {
    KeycloakTestClient keycloakTestClient = new KeycloakTestClient();

    @Test
    void searchHelpItemByProductNameAndItemIdTest() {

        var data = given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(keycloakTestClient.getClientAccessToken("read-only"))
                .pathParam("productName", "productName")
                .pathParam("helpItemId", "cg")
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(HelpDTOV1.class);

        assertThat(data).isNotNull();
        Assertions.assertEquals("productName", data.getProductName());
        Assertions.assertEquals("cg", data.getItemId());
    }

    @Test
    void searchByProductNameAndItemIdNoFoundTest() {

        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(keycloakTestClient.getClientAccessToken("read-only"))
                .pathParam("productName", "does-not-exists")
                .pathParam("helpItemId", "cg")
                .get()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

}
