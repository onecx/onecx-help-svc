package io.github.onecx.help.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import gen.io.github.onecx.help.rs.internal.model.*;
import io.github.onecx.help.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestHTTPEndpoint(HelpsRestController.class)
@WithDBData(value = "data/testdata-internal-tenant.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@TestProfile(AbstractTest.TenantTestProfile.class)
class HelpsRestControllerTenantTest extends AbstractTest {

    @Test
    void createNewHelpTest() {

        // create help
        var helpDto = new CreateHelpDTO();
        helpDto.setItemId("test01");
        helpDto.setAppId("appId");
        helpDto.setContext("context");
        helpDto.setResourceUrl("resource/url");
        helpDto.setBaseUrl("base/url");

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .body(helpDto)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract()
                .body().as(HelpDTO.class);

        given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org2"))
                .get(dto.getId())
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        dto = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get(dto.getId())
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
                .header(APM_HEADER_PARAM, createToken("org1"))
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo("CONSTRAINT_VIOLATIONS");
        assertThat(exception.getDetail()).isEqualTo("createNewHelp.createHelpDTO: must not be null");

        // create help with existing itemId
        helpDto = new CreateHelpDTO();
        helpDto.setItemId("cg");

        exception = given().when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .body(helpDto)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo("PERSIST_ENTITY_FAILED");
        assertThat(exception.getDetail()).isEqualTo(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'help_item_id'  Detail: Key (item_id, tenant_id)=(cg, tenant-100) already exists.]");
    }

    @Test
    void deleteHelpTest() {

        // delete entity with wrong tenant
        given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .delete("DELETE_1")
                .then().statusCode(NO_CONTENT.getStatusCode());

        // delete entity with wrong tenant still exists
        given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org2"))
                .get("DELETE_1")
                .then().statusCode(OK.getStatusCode());

        // delete help
        given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org2"))
                .delete("DELETE_1")
                .then().statusCode(NO_CONTENT.getStatusCode());

        // check if help exists
        given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org2"))
                .get("DELETE_1")
                .then().statusCode(NOT_FOUND.getStatusCode());

        // delete help in portal
        given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org2"))
                .delete("11-111")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

    }

    @Test
    void getHelpByHelpDefinitionNameTest() {
        var dto = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
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
                .header(APM_HEADER_PARAM, createToken("org2"))
                .pathParam("itemId", "helpWithoutPortal")
                .get("/itemId/{itemId}")
                .then().statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void getHelpByIdTest() {

        var dto = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
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
                .get("22-222")
                .then().statusCode(NOT_FOUND.getStatusCode());

        dto = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get("11-111")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(HelpDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getItemId()).isEqualTo("cg");
        assertThat(dto.getId()).isEqualTo("11-111");

    }

    @Test
    void getHelpsNoTenantTest() {
        var data = given()
                .contentType(APPLICATION_JSON)
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(HelpPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isZero();
        assertThat(data.getStream()).isNotNull().isEmpty();

    }

    @Test
    void getHelpsTest() {
        var data = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(HelpPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(2);
        assertThat(data.getStream()).isNotNull().hasSize(2);

        data = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org2"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(HelpPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(1);
        assertThat(data.getStream()).isNotNull().hasSize(1);

    }

    @Test
    void searchHelpsTest() {
        var criteria = new HelpSearchCriteriaDTO();

        var data = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(HelpPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(2);
        assertThat(data.getStream()).isNotNull().hasSize(2);

        criteria.setItemId(" ");
        data = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(HelpPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(2);
        assertThat(data.getStream()).isNotNull().hasSize(2);

        criteria.setItemId("cg");
        data = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
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

    }

    @Test
    void updateHelpTest() {

        // update none existing help
        var helpDto = new UpdateHelpDTO();
        helpDto.setItemId("test01");
        helpDto.setContext("context-update");

        given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org2"))
                .body(helpDto)
                .when()
                .put("11-111")
                .then().statusCode(NOT_FOUND.getStatusCode());

        // update help
        given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .body(helpDto)
                .when()
                .put("11-111")
                .then().statusCode(NO_CONTENT.getStatusCode());

        // download help
        var dto = given().contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .body(helpDto)
                .when()
                .get("11-111")
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
        helpDto.setContext("context");

        var exception = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .when()
                .body(helpDto)
                .put("11-111")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(exception);
        Assertions.assertEquals("MERGE_ENTITY_FAILED", exception.getErrorCode());
        Assertions.assertEquals(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'help_item_id'  Detail: Key (item_id, tenant_id)=(helpWithoutPortal, tenant-100) already exists.]",
                exception.getDetail());
        Assertions.assertNull(exception.getInvalidParams());

    }

    @Test
    void updateHelpWithoutBodyTest() {

        var exception = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org2"))
                .when()
                .put("update_create_new")
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

}
