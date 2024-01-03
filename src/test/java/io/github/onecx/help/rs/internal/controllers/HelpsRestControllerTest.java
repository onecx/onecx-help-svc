package io.github.onecx.help.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import gen.io.github.onecx.help.rs.internal.model.*;
import io.github.onecx.help.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(HelpsRestController.class)
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class HelpsRestControllerTest extends AbstractTest {

    @Test
    void createNewHelpTest() {

        // create help
        var helpDto = new CreateHelpDTO();
        helpDto.setItemId("test01");
        helpDto.setAppId("appId");
        helpDto.setContext("context");
        helpDto.setResourceUrl("resource/url");
        helpDto.setBaseUrl("base/url");

        var uri = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(helpDto)
                .post()
                .then().statusCode(CREATED.getStatusCode())
                .extract().header(HttpHeaders.LOCATION);

        var dto = given()
                .contentType(APPLICATION_JSON)
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
                .returns(helpDto.getAppId(), from(HelpDTO::getAppId));

        // create help without body
        var exception = given()
                .when()
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo("CONSTRAINT_VIOLATIONS");
        assertThat(exception.getDetail()).isEqualTo("createNewHelp.createHelpDTO: must not be null");

        // create help with existing name
        helpDto = new CreateHelpDTO();
        helpDto.setItemId("cg");
        helpDto.setAppId("appId1");

        exception = given().when()
                .contentType(APPLICATION_JSON)
                .body(helpDto)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo("PERSIST_ENTITY_FAILED");
        assertThat(exception.getDetail()).isEqualTo(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'help_item_id'  Detail: Key (item_id, app_id, tenant_id)=(cg, appId1, default) already exists.]");
    }

    @Test
    void deleteHelpTest() {

        // delete help
        given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "DELETE_1")
                .delete("{id}")
                .then().statusCode(NO_CONTENT.getStatusCode());

        // check if help exists
        given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "DELETE_1")
                .get("{id}")
                .then().statusCode(NOT_FOUND.getStatusCode());

        // delete help in portal
        given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .delete("{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

    }

    @Test
    void getHelpByHelpDefinitionNameTest() {
        var dto = given()
                .contentType(APPLICATION_JSON)
                .pathParam("itemId", "helpWithoutPortal")
                .get("/itemId/{itemId}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(HelpDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getItemId()).isEqualTo("helpWithoutPortal");
        assertThat(dto.getId()).isEqualTo("22-222");

        given()
                .contentType(APPLICATION_JSON)
                .pathParam("itemId", "none-exists")
                .get("/itemId/{itemId}")
                .then().statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void getHelpByIdTest() {

        var dto = given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "22-222")
                .get("{id}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(HelpDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getItemId()).isEqualTo("helpWithoutPortal");
        assertThat(dto.getId()).isEqualTo("22-222");

        given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "___")
                .get("{id}")
                .then().statusCode(NOT_FOUND.getStatusCode());

        dto = given()
                .contentType(APPLICATION_JSON)
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
    void getHelpsTest() {
        var data = given()
                .contentType(APPLICATION_JSON)
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(HelpPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(3);
        assertThat(data.getStream()).isNotNull().hasSize(3);

    }

    @Test
    void searchHelpsTest() {
        var criteria = new HelpSearchCriteriaDTO();

        var data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(HelpPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(3);
        assertThat(data.getStream()).isNotNull().hasSize(3);

        criteria.setItemId(" ");
        criteria.setResourceUrl(" ");
        criteria.setBaseUrl(" ");
        criteria.setContext(" ");
        criteria.setAppId(" ");
        data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(HelpPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(3);
        assertThat(data.getStream()).isNotNull().hasSize(3);

        criteria.setItemId("cg");
        criteria.setResourceUrl("test1");
        criteria.setBaseUrl("test1");
        criteria.setContext("test1");
        criteria.setAppId("appId1");

        data = given()
                .contentType(APPLICATION_JSON)
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
        helpDto.setItemId("test01");
        helpDto.setContext("context-update");

        given()
                .contentType(APPLICATION_JSON)
                .body(helpDto)
                .when()
                .pathParam("id", "does-not-exists")
                .put("{id}")
                .then().statusCode(NOT_FOUND.getStatusCode());

        // update help
        given()
                .contentType(APPLICATION_JSON)
                .body(helpDto)
                .when()
                .pathParam("id", "11-111")
                .put("{id}")
                .then().statusCode(NO_CONTENT.getStatusCode());

        // download help
        var dto = given().contentType(APPLICATION_JSON)
                .body(helpDto)
                .when()
                .pathParam("id", "11-111")
                .get("{id}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(HelpDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getContext()).isEqualTo(helpDto.getContext());

    }

    @Test
    void updateHelpWithExistingItemIdTest() {

        var helpDto = new UpdateHelpDTO();
        helpDto.setItemId("helpWithoutPortal");
        helpDto.setAppId("appId");
        helpDto.setContext("context");

        var exception = given()
                .contentType(APPLICATION_JSON)
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
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'help_item_id'  Detail: Key (item_id, app_id, tenant_id)=(helpWithoutPortal, appId, default) already exists.]",
                exception.getDetail());
        Assertions.assertNull(exception.getInvalidParams());

    }

    @Test
    void updateHelpWithoutBodyTest() {

        var exception = given()
                .contentType(APPLICATION_JSON)
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
    void getAllAppsWithHelpItemsTest() {
        var output = given()
                .contentType(APPLICATION_JSON)
                .when()
                .get("/appIds")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(HelpAppIdsDTO.class);
        Assertions.assertNotNull(output);
        Assertions.assertEquals(output.getAppIds().size(), 2);
        Assertions.assertEquals(output.getAppIds().get(0), "appId");
    }
}
