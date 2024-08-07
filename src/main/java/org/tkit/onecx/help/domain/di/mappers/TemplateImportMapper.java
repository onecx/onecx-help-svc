package org.tkit.onecx.help.domain.di.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.help.domain.models.Help;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.help.di.template.model.TemplateHelpItemDTO;

@Mapper(uses = OffsetDateTimeMapper.class)
public interface TemplateImportMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "operator", constant = "false")
    Help create(String productName, String itemId, TemplateHelpItemDTO object);

    static String prId(Help pr) {
        return prId(pr.getProductName(), pr.getItemId());
    }

    static String prId(String productName, String itemId) {
        return productName + "#" + itemId;
    }
}
