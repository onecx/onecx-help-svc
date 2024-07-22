package org.tkit.onecx.help.domain.di.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.help.domain.models.Help;
import org.tkit.onecx.help.domain.models.ProductResource;
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
    @Mapping(target = "operator", ignore = true)
    ProductResource createProductResource(String productName, String itemId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "operator", ignore = true)
    @Mapping(target = "productResource", ignore = true)
    @Mapping(target = "resourceRefId", ignore = true)
    Help create(TemplateHelpItemDTO object);

    static String prId(ProductResource pr) {
        return prId(pr.getProductName(), pr.getItemId());
    }

    static String prId(String productName, String itemId) {
        return productName + "#" + itemId;
    }
}
