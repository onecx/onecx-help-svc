package org.tkit.onecx.help.rs.exim.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.tkit.quarkus.security.test.SecurityTestUtils.getKeycloakClientToken;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.help.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.help.rs.exim.v1.model.EximHelpDTOV1;
import gen.org.tkit.onecx.help.rs.exim.v1.model.ExportHelpsRequestDTOV1;
import gen.org.tkit.onecx.help.rs.exim.v1.model.HelpSnapshotDTOV1;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(HelpEximRestControllerV1.class)
@WithDBData(value = "data/test-exim-v1.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-hp:write", "ocx-hp:read" })
class HelpEximRestControllerV1Test extends AbstractTest {

    @Test
    void exportEmptyRequestHelpsTest() {
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .post("/export")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    void exportAllHelpsTest() {
        ExportHelpsRequestDTOV1 request = new ExportHelpsRequestDTOV1();
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(HelpSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getHelps()).isNotNull().hasSize(3);
        var w = dto.getHelps().get("productName");
        assertThat(w).isNotNull().containsOnlyKeys("toDelete", "helpWithoutPortal");

        request.setProductNames(null);
        dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(HelpSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getHelps()).isNotNull().hasSize(3);
    }

    @Test
    void exportHelpsTest() {
        ExportHelpsRequestDTOV1 request = new ExportHelpsRequestDTOV1();
        request.addProductNamesItem("productName1").addProductNamesItem("productName");
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(HelpSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getHelps()).isNotNull().hasSize(2);
        var w = dto.getHelps().get("productName");
        assertThat(w).isNotNull().containsOnlyKeys("toDelete", "helpWithoutPortal");

    }

    @Test
    void importHelpsTest() {

        ExportHelpsRequestDTOV1 request = new ExportHelpsRequestDTOV1();
        request.addProductNamesItem("productName1").addProductNamesItem("productName");
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(HelpSnapshotDTOV1.class);

        dto.getHelps().get("productName").get("helpWithoutPortal").setBaseUrl("new-custom-url");
        dto.getHelps().put("new-product", Map.of(
                "item1", new EximHelpDTOV1().baseUrl("1"),
                "item2", new EximHelpDTOV1().baseUrl("2"),
                "item3", new EximHelpDTOV1().baseUrl("3")));

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(dto)
                .post("/import")
                .then()
                .statusCode(OK.getStatusCode());

        request.addProductNamesItem("new-product");
        dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(HelpSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getHelps()).isNotNull().hasSize(3);
        var w = dto.getHelps().get("productName");
        assertThat(w).isNotNull().containsOnlyKeys("toDelete", "helpWithoutPortal");
        assertThat(w.get("helpWithoutPortal")).isNotNull();
        assertThat(w.get("helpWithoutPortal").getBaseUrl()).isEqualTo("new-custom-url");

        w = dto.getHelps().get("new-product");
        assertThat(w).isNotNull().containsOnlyKeys("item1", "item2", "item3");
    }

}
