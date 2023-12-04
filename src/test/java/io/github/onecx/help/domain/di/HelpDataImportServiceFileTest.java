package io.github.onecx.help.domain.di;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.onecx.help.domain.daos.HelpDAO;
import io.github.onecx.help.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@DisplayName("Help data import test from example file")
@TestProfile(HelpDataImportServiceFileTest.CustomProfile.class)
class HelpDataImportServiceFileTest extends AbstractTest {

    @Inject
    HelpDAO dao;

    @Test
    @DisplayName("Import help data from file")
    void importDataFromFileTest() {
        var data = dao.findAll().toList();
        assertThat(data).isNotNull().hasSize(2);
    }

    public static class CustomProfile implements QuarkusTestProfile {

        @Override
        public String getConfigProfile() {
            return "test";
        }

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "tkit.dataimport.enabled", "true",
                    "tkit.dataimport.configurations.help.enabled", "true",
                    "tkit.dataimport.configurations.help.file", "./src/test/resources/import/help-import.json",
                    "tkit.dataimport.configurations.help.metadata.operation", "CLEAN_INSERT",
                    "tkit.dataimport.configurations.help.stop-at-error", "true");
        }
    }

}
