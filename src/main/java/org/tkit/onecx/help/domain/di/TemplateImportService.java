package org.tkit.onecx.help.domain.di;

import static org.tkit.onecx.help.domain.di.mappers.TemplateImportMapper.prId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.help.domain.di.mappers.TemplateImportMapper;
import org.tkit.onecx.help.domain.models.Help;
import org.tkit.onecx.help.domain.models.ProductResource;
import org.tkit.quarkus.dataimport.DataImport;
import org.tkit.quarkus.dataimport.DataImportConfig;
import org.tkit.quarkus.dataimport.DataImportService;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.help.di.template.model.TemplateHelpItemDTO;
import gen.org.tkit.onecx.help.di.template.model.TemplateImportDTO;

@DataImport("template")
public class TemplateImportService implements DataImportService {

    private static final Logger log = LoggerFactory.getLogger(TemplateImportService.class);

    @Inject
    HelpImportService importService;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    TemplateImportMapper mapper;

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void importData(DataImportConfig config) {
        log.info("Import help from configuration {}", config);
        try {
            List<String> tenants = List.of();
            var tmp = config.getMetadata().get("tenants");
            if (tmp != null) {
                tenants = List.of(tmp.split(","));
            }

            if (tenants.isEmpty()) {
                log.warn("No tenants defined for the templates");
                return;
            }

            if (config.getData() == null || config.getData().length == 0) {
                log.warn("Import configuration key {} does not contains any data to import", config.getKey());
                return;
            }

            TemplateImportDTO data = objectMapper.readValue(config.getData(), TemplateImportDTO.class);

            if (data.getHelps() == null || data.getHelps().isEmpty()) {
                log.warn("Import configuration key {} does not contains any JSON data to import", config.getKey());
                return;
            }

            // create product resources
            var pr = importProductResources(data.getHelps());

            // execute the import help
            importHelps(tenants, data.getHelps(), pr);
        } catch (Exception ex) {
            throw new ImportException(ex.getMessage(), ex);
        }
    }

    private Map<String, ProductResource> importProductResources(Map<String, Map<String, TemplateHelpItemDTO>> data) {

        var existingData = importService.getExistingProductResources();

        List<ProductResource> createData = new ArrayList<>();
        data.forEach((productName, pn) -> pn.forEach((itemId, value) -> {
            if (!existingData.containsKey(prId(productName, itemId))) {
                createData.add(mapper.createProductResource(productName, itemId));
            }
        }));

        var tmp = importService.createProductResources(createData);
        existingData.putAll(tmp);

        return existingData;
    }

    private void importHelps(List<String> tenants, Map<String, Map<String, TemplateHelpItemDTO>> data,
            Map<String, ProductResource> pr) {

        tenants.forEach(tenant -> {
            // load helps from the database
            var existingData = importService.getData(tenant);

            List<Help> createData = new ArrayList<>();
            data.forEach((productName, pn) -> pn.forEach((itemId, value) -> {
                var id = prId(productName, itemId);
                if (!existingData.containsKey(id)) {
                    var tmp = mapper.create(value);
                    tmp.setProductResource(pr.get(id));
                    createData.add(tmp);
                }
            }));

            // create themes in database for tenant
            importService.importHelp(tenant, createData);
        });
    }

    public static class ImportException extends RuntimeException {

        public ImportException(String message, Throwable ex) {
            super(message, ex);
        }
    }
}
