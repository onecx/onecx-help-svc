package io.github.onecx.help.rs.internal.mappers;

import java.util.List;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gen.io.github.onecx.help.rs.internal.model.*;
import io.github.onecx.help.domain.criteria.HelpSearchCriteria;
import io.github.onecx.help.domain.models.Help;

@Mapper(uses = { OffsetDateTimeMapper.class })
public abstract class HelpMapper {

    @Inject
    ObjectMapper mapper;

    public abstract HelpSearchCriteria map(HelpSearchCriteriaDTO dto);

    @Mapping(target = "removeStreamItem", ignore = true)
    public abstract HelpPageResultDTO mapPage(PageResult<Help> page);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    public abstract Help create(CreateHelpDTO object);

    public abstract List<HelpDTO> map(Stream<Help> entity);

    @Mapping(target = "version", source = "modificationCount")
    public abstract HelpDTO map(Help help);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    public abstract void update(UpdateHelpDTO helpDTO, @MappingTarget Help entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    public abstract Help map(UpdateHelpDTO object);

    @Named("properties")
    public String mapToString(Object properties) {

        if (properties == null) {
            return null;
        }

        try {
            return mapper.writeValueAsString(properties);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public HelpAppIdsDTO map(List<String> appIds) {
        HelpAppIdsDTO appIdsDTO = new HelpAppIdsDTO();
        appIdsDTO.setAppIds(appIds);
        return appIdsDTO;
    }

}
