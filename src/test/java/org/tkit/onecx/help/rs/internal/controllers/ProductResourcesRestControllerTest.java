package org.tkit.onecx.help.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.tkit.quarkus.security.test.SecurityTestUtils.getKeycloakClientToken;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.help.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.help.rs.internal.model.CreateProductResourceDTO;
import gen.org.tkit.onecx.help.rs.internal.model.ProductResourceDTO;
import gen.org.tkit.onecx.help.rs.internal.model.ProductResourceItemsDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ProductResourcesRestController.class)
@WithDBData(value = "data/test-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = "ocx-hp:all")
class ProductResourcesRestControllerTest extends AbstractTest {

    @Test
    void createNewProductResourceTest() {

        // create help
        var request = new CreateProductResourceDTO();

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .body(request)
                .post()
                .then().statusCode(BAD_REQUEST.getStatusCode());

        request.productName("test-p1").itemId("test-item1");

        var uri = given()
                .when()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .body(request)
                .post()
                .then().statusCode(CREATED.getStatusCode())
                .extract().header(HttpHeaders.LOCATION);

        var dto = given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .get(uri)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().as(ProductResourceDTO.class);

        assertThat(dto).isNotNull()
                .returns(request.getItemId(), from(ProductResourceDTO::getItemId))
                .returns(false, from(ProductResourceDTO::getOperator))
                .returns(request.getProductName(), from(ProductResourceDTO::getProductName));
    }

    @Test
    void deleteProductResourceTest() {

        // delete product resource
        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .delete("DELETE_1}")
                .then().statusCode(NO_CONTENT.getStatusCode());

        // check if help exists
        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .get("DELETE_1")
                .then().statusCode(NOT_FOUND.getStatusCode());

        // delete use product resource
        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .delete("p1")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        // delete not use product resource
        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .delete("p4")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void getProductResourceByIdTest() {

        var dto = given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .get("p1")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(ProductResourceDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getProductName()).isEqualTo("productName1");
        assertThat(dto.getId()).isEqualTo("p1");

        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .get("-------")
                .then().statusCode(NOT_FOUND.getStatusCode());

    }

    @Test
    void getAllProductResourcesTest() {
        var dto = given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .get()
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(ProductResourceItemsDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotNull().hasSize(2);

    }
}
