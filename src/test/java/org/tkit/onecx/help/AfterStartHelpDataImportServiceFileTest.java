package org.tkit.onecx.help;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.help.domain.daos.HelpDAO;
import org.tkit.onecx.help.domain.daos.ProductResourceDAO;
import org.tkit.onecx.help.test.AbstractTest;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@DisplayName("Help data import test from example file")
class AfterStartHelpDataImportServiceFileTest extends AbstractTest {

    @Inject
    HelpDAO dao;

    @Inject
    ProductResourceDAO productResourceDAO;

    @Test
    @DisplayName("Import help data from file")
    void importDataFromFileTest() {

        var pr = productResourceDAO.findAll().toList();
        assertThat(pr).isNotNull().hasSize(4);

        var data = dao.findAll().toList();
        assertThat(data).isNotNull().hasSize(4);
    }

}
