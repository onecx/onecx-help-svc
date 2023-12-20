package io.github.onecx.help.rs.v1.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.help.rs.v1.model.HelpDTOV1;
import io.github.onecx.help.domain.models.Help;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface HelpMapper {
    @Mapping(source = "modificationCount", target = "version")
    HelpDTOV1 map(Help helpItem);
}
