package org.tkit.onecx.help.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.help.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.help.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(HelpsRestController.class)
@WithDBData(value = "data/test-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = "ocx-hp:all")
class HelpsRestControllerTest extends AbstractTest {
    @Test
    void createNewHelpTest() {

        // create help
        var helpDto = new CreateHelpDTO();
        helpDto.setItemId("test01");
        helpDto.setProductName("productName");
        helpDto.setContext("context");
        helpDto.setResourceUrl("resource/url");
        helpDto.setBaseUrl("base/url");

        var uri = given()
                .when()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .body(helpDto)
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
                .body().as(HelpDTO.class);

        assertThat(dto).isNotNull()
                .returns(helpDto.getItemId(), from(HelpDTO::getItemId))
                .returns(helpDto.getContext(), from(HelpDTO::getContext))
                .returns(helpDto.getBaseUrl(), from(HelpDTO::getBaseUrl))
                .returns(helpDto.getResourceUrl(), from(HelpDTO::getResourceUrl))
                .returns(helpDto.getProductName(), from(HelpDTO::getProductName));

        // create help without body
        var exception = given()
                .when()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo("CONSTRAINT_VIOLATIONS");
        assertThat(exception.getDetail()).isEqualTo("createNewHelp.createHelpDTO: must not be null");

        // create help with existing name
        helpDto = new CreateHelpDTO();
        helpDto.setItemId("cg");
        helpDto.setProductName("productName1");

        exception = given().when()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .body(helpDto)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo("PERSIST_ENTITY_FAILED");
        assertThat(exception.getDetail()).isEqualTo(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'help_item_id'  Detail: Key (item_id, product_name, tenant_id)=(cg, productName1, default) already exists.]");
    }

    @Test
    void deleteHelpTest() {

        // delete help
        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("id", "DELETE_1")
                .delete("{id}")
                .then().statusCode(NO_CONTENT.getStatusCode());

        // check if help exists
        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("id", "DELETE_1")
                .get("{id}")
                .then().statusCode(NOT_FOUND.getStatusCode());

        // delete help in portal
        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("id", "11-111")
                .delete("{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

    }

    @Test
    void getHelpByIdTest() {

        var dto = given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .get("22-222")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(HelpDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getItemId()).isEqualTo("helpWithoutPortal");
        assertThat(dto.getId()).isEqualTo("22-222");

        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("id", "___")
                .get("{id}")
                .then().statusCode(NOT_FOUND.getStatusCode());

        dto = given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("id", "11-111")
                .get("{id}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(HelpDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getItemId()).isEqualTo("cg");
        assertThat(dto.getId()).isEqualTo("11-111");

    }

    @Test
    void searchHelpsTest() {
        var criteria = new HelpSearchCriteriaDTO();

        var data = given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(HelpPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(4);
        assertThat(data.getStream()).isNotNull().hasSize(4);

        criteria.setItemId("");
        criteria.setResourceUrl(" ");
        criteria.setBaseUrl(" ");
        criteria.setContext(" ");
        criteria.setProductName(" ");
        data = given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(HelpPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(4);
        assertThat(data.getStream()).isNotNull().hasSize(4);

        criteria.setItemId("cg");
        criteria.setResourceUrl("test1");
        criteria.setBaseUrl("test1");
        criteria.setContext("test1");
        criteria.setProductName("productName1");

        data = given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(HelpPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(1);
        assertThat(data.getStream()).isNotNull().hasSize(1);
        assertThat(data.getStream().get(0).getContext()).isEqualTo(criteria.getContext());
        assertThat(data.getStream().get(0).getResourceUrl()).isEqualTo(criteria.getResourceUrl());
        assertThat(data.getStream().get(0).getBaseUrl()).isEqualTo(criteria.getBaseUrl());
    }

    @Test
    void updateHelpTest() {

        // update none existing help
        var helpDto = new UpdateHelpDTO();
        helpDto.setModificationCount(0);
        helpDto.setItemId("test01");
        helpDto.setContext("context-update");

        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .body(helpDto)
                .when()
                .pathParam("id", "does-not-exists")
                .put("{id}")
                .then().statusCode(NOT_FOUND.getStatusCode());

        // update help
        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .body(helpDto)
                .when()
                .pathParam("id", "11-111")
                .put("{id}")
                .then().statusCode(NO_CONTENT.getStatusCode());

        // download help
        var dto = given().contentType(APPLICATION_JSON)
                .body(helpDto)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .pathParam("id", "11-111")
                .get("{id}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(HelpDTO.class);

        // update theme with wrong modificationCount
        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .body(helpDto)
                .when()
                .pathParam("id", "11-111")
                .put("{id}")
                .then().statusCode(BAD_REQUEST.getStatusCode());

        assertThat(dto).isNotNull();
        assertThat(dto.getContext()).isEqualTo(helpDto.getContext());

    }

    @Test
    void updateHelpWithExistingItemIdTest() {

        var helpDto = new UpdateHelpDTO();
        helpDto.setModificationCount(0);
        helpDto.setItemId("helpWithoutPortal");
        helpDto.setProductName("productName");
        helpDto.setContext("context");

        var exception = given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .body(helpDto)
                .pathParam("id", "11-111")
                .put("{id}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(exception);
        Assertions.assertEquals("MERGE_ENTITY_FAILED", exception.getErrorCode());
        Assertions.assertEquals(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'help_item_id'  Detail: Key (item_id, product_name, tenant_id)=(helpWithoutPortal, productName, default) already exists.]",
                exception.getDetail());
        Assertions.assertTrue(exception.getInvalidParams().isEmpty());

    }

    @Test
    void updateHelpWithoutBodyTest() {

        var exception = given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .pathParam("id", "update_create_new")
                .put("{id}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(exception);
        Assertions.assertEquals("CONSTRAINT_VIOLATIONS", exception.getErrorCode());
        Assertions.assertEquals("updateHelp.updateHelpDTO: must not be null",
                exception.getDetail());
        Assertions.assertNotNull(exception.getInvalidParams());
        Assertions.assertEquals(1, exception.getInvalidParams().size());
    }

    @Test
    //@CreateKeycloakClient(name=, secret=, scopes=, deleteAfter)
    void getAllAppsWithHelpItemsTest() {
        var output = given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .get("/productNames")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(HelpProductNamesDTO.class);
        Assertions.assertNotNull(output);
        Assertions.assertEquals(3, output.getProductNames().size());
        assertThat(output.getProductNames()).contains("productName");
    }

    @Test
    void searchHelpItemByProductNameAndItemIdTest() {

        var data = given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .get("productName1/cg")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(HelpDTO.class);

        assertThat(data).isNotNull();
        Assertions.assertEquals("productName1", data.getProductName());
        Assertions.assertEquals("cg", data.getItemId());
    }

    @Test
    void searchByProductNameAndItemIdNoFoundTest() {

        given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .get("does-not-exists/cg")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void searchHelpItemByProductNameAndItemIdProductUrlTest() {

        var data = given()
                .contentType(APPLICATION_JSON)
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .get("no-page-product/cg")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(HelpDTO.class);

        assertThat(data).isNotNull();
        Assertions.assertEquals("no-page-product", data.getProductName());
        Assertions.assertEquals("PRODUCT_BASE_DOC_URL", data.getItemId());
    }
}
