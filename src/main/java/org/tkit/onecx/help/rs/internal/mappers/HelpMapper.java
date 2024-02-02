package org.tkit.onecx.help.rs.internal.mappers;

import java.util.List;
import java.util.stream.Stream;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.tkit.onecx.help.domain.criteria.HelpSearchCriteria;
import org.tkit.onecx.help.domain.models.Help;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.help.rs.internal.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface HelpMapper {

    HelpSearchCriteria map(HelpSearchCriteriaDTO dto);

    @Mapping(target = "removeStreamItem", ignore = true)
    HelpPageResultDTO mapPage(PageResult<Help> page);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    Help create(CreateHelpDTO object);

    List<HelpDTO> map(Stream<Help> entity);

    HelpDTO map(Help help);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    void update(UpdateHelpDTO helpDTO, @MappingTarget Help entity);

    default HelpAppIdsDTO map(List<String> appIds) {
        if (appIds == null) {
            return null;
        }
        HelpAppIdsDTO appIdsDTO = new HelpAppIdsDTO();
        appIdsDTO.setAppIds(appIds);
        return appIdsDTO;
    }

}
