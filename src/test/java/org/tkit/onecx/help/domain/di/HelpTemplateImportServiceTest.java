package org.tkit.onecx.help.domain.di;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.help.domain.daos.HelpDAO;
import org.tkit.onecx.help.domain.models.Help;
import org.tkit.onecx.help.test.AbstractTest;
import org.tkit.quarkus.dataimport.DataImportConfig;
import org.tkit.quarkus.test.WithDBData;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.help.di.template.model.TemplateHelpItemDTO;
import gen.org.tkit.onecx.help.di.template.model.TemplateImportDTO;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithDBData(value = "data/test-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class HelpTemplateImportServiceTest extends AbstractTest {

    @Inject
    TemplateImportService service;

    @Inject
    HelpDAO dao;

    @Inject
    ObjectMapper mapper;

    @Test
    void importDataNotSupportedActionTest() {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("operation", "CUSTOM_NOT_SUPPORTED");
        DataImportConfig config = new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return metadata;
            }
        };

        service.importData(config);

        List<Help> data = dao.findAll().toList();
        assertThat(data).isNotNull().hasSize(4);

    }

    @Test
    void importEmptyDataTest() {
        Assertions.assertDoesNotThrow(() -> {
            service.importData(new DataImportConfig() {
                @Override
                public Map<String, String> getMetadata() {
                    return Map.of("tenants", "default");
                }
            });
        });
        Assertions.assertDoesNotThrow(() -> {
            service.importData(new DataImportConfig() {
                @Override
                public Map<String, String> getMetadata() {
                    return Map.of("tenants", "default");
                }

                @Override
                public byte[] getData() {
                    return new byte[] {};
                }
            });
        });
        Assertions.assertDoesNotThrow(() -> {
            service.importData(new DataImportConfig() {
                @Override
                public Map<String, String> getMetadata() {
                    return Map.of("tenants", "default");
                }

                @Override
                public byte[] getData() {
                    try {
                        return mapper.writeValueAsBytes(new TemplateImportDTO());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });

            service.importData(new DataImportConfig() {
                @Override
                public Map<String, String> getMetadata() {
                    return Map.of("tenants", "default");
                }

                @Override
                public byte[] getData() {
                    try {
                        var data = new TemplateImportDTO();
                        data.setHelps(null);
                        return mapper.writeValueAsBytes(data);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });

        });

        var config = new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return Map.of("tenants", "default");
            }

            @Override
            public byte[] getData() {
                return new byte[] { 0 };
            }
        };
        Assertions.assertThrows(TemplateImportService.ImportException.class, () -> service.importData(config));

    }

    @Test
    void importDataExistTest() {

        TemplateImportDTO request = new TemplateImportDTO()
                .putHelpsItem("productName1", Map.of("cg", new TemplateHelpItemDTO().context("test1")))
                .putHelpsItem("pi", Map.of("i1", new TemplateHelpItemDTO().context("test1")));

        DataImportConfig config = new DataImportConfig() {
            @Override
            public Map<String, String> getMetadata() {
                return Map.of("tenants", "default");
            }

            @Override
            public byte[] getData() {
                try {
                    return mapper.writeValueAsBytes(request);
                } catch (Exception ex) {
                    return null;
                }
            }
        };

        service.importData(config);

        List<Help> data = dao.findAll().toList();
        assertThat(data).isNotNull().hasSize(5);

    }
}