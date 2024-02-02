package org.tkit.onecx.help.rs.v1.mappers;

import org.mapstruct.Mapper;
import org.tkit.onecx.help.domain.models.Help;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.help.rs.v1.model.HelpDTOV1;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ExternalV1Mapper {

    HelpDTOV1 map(Help data);
}
