package io.github.onecx.help.rs.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import gen.io.github.onecx.help.rs.v1.model.HelpDTOV1;
import gen.io.github.onecx.help.rs.v1.model.HelpSearchCriteriaDTOV1;
import gen.io.github.onecx.help.rs.v1.model.ProblemDetailResponseDTOV1;
import io.github.onecx.help.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(HelpsV1RestController.class)
@WithDBData(value = "data/testdata-v1.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class HelpsV1RestControllerTest extends AbstractTest {
    @Test
    void searchHelpItemByAppIdAndItemIdTest() {

        var criteria = new HelpSearchCriteriaDTOV1();
        criteria.setAppId("appId");
        criteria.setItemId("cg");

        var data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(HelpDTOV1.class);

        assertThat(data).isNotNull();
        Assertions.assertEquals(data.getAppId(), criteria.getAppId());
        Assertions.assertEquals(data.getItemId(), criteria.getItemId());
    }

    @Test
    void searchHelpItemByAppIdAndItemIdBlankInputTest() {

        var criteria = new HelpSearchCriteriaDTOV1();
        criteria.setAppId(" ");
        criteria.setItemId(" ");

        var data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post()
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract();

        assertThat(data).isNotNull();

        criteria.setAppId(null);
        criteria.setItemId(null);

        data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract();

        assertThat(data).isNotNull();
    }

    @Test
    void searchHelpItemByAppIdAndItemIdTestFail() {

        var criteria = new HelpSearchCriteriaDTOV1();
        var data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(ProblemDetailResponseDTOV1.class);

        assertThat(data).isNotNull();
        Assertions.assertEquals("CONSTRAINT_VIOLATIONS", data.getErrorCode());
    }

}
