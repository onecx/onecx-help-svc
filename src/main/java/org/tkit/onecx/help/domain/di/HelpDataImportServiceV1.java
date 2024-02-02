package org.tkit.onecx.help.domain.di;

import java.util.function.Consumer;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.quarkus.dataimport.DataImport;
import org.tkit.quarkus.dataimport.DataImportConfig;
import org.tkit.quarkus.dataimport.DataImportService;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.help.di.v1.model.DataImportDTOV1;

@DataImport("help")
public class HelpDataImportServiceV1 implements DataImportService {

    private static final Logger log = LoggerFactory.getLogger(HelpDataImportServiceV1.class);

    @Inject
    HelmImportService service;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public void importData(DataImportConfig config) {
        log.info("Import help from configuration {}", config);
        try {
            var operation = config.getMetadata().getOrDefault("operation", "NONE");

            Consumer<DataImportDTOV1> action = null;
            if ("CLEAN_INSERT".equals(operation)) {
                action = this::cleanInsert;
            }

            if (action == null) {
                log.warn("Not supported operation '{}' for the import configuration key '{}'", operation, config.getKey());
                return;
            }

            if (config.getData() == null || config.getData().length == 0) {
                log.warn("Import configuration key {} does not contains any data to import", config.getKey());
                return;
            }

            DataImportDTOV1 data = objectMapper.readValue(config.getData(), DataImportDTOV1.class);

            if (data.getHelps() == null || data.getHelps().isEmpty()) {
                log.warn("Import configuration key {} does not contains any JSON data to import", config.getKey());
                return;
            }

            // execute the import
            action.accept(data);
        } catch (Exception ex) {
            throw new ImportException(ex.getMessage(), ex);
        }
    }

    public void cleanInsert(DataImportDTOV1 data) {

        // clean data
        service.deleteAllCommonData();

        // import helps
        data.getHelps().forEach((tenantId, helps) -> service.importTenantHelp(tenantId, helps));
    }

    public static class ImportException extends RuntimeException {

        public ImportException(String message, Throwable ex) {
            super(message, ex);
        }
    }
}
