package org.tkit.onecx.help.domain.di.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.help.domain.models.Help;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.help.di.v1.model.DataImportHelpDTOV1;

@Mapper(uses = OffsetDateTimeMapper.class)
public interface DataImportMapperV1 {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    Help create(DataImportHelpDTOV1 dto, String itemId);

}
