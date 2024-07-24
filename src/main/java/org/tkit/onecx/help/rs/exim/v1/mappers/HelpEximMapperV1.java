package org.tkit.onecx.help.rs.exim.v1.mappers;

import static java.util.stream.Collectors.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.tkit.onecx.help.domain.models.Help;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.help.rs.exim.v1.model.EximHelpDTOV1;
import gen.org.tkit.onecx.help.rs.exim.v1.model.HelpSnapshotDTOV1;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface HelpEximMapperV1 {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "operator", constant = "true")
    Help createHelp(String productName, String itemId, EximHelpDTOV1 dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "operator", constant = "true")
    @Mapping(target = "itemId", ignore = true)
    @Mapping(target = "productName", ignore = true)
    Help updateHelp(EximHelpDTOV1 dto, @MappingTarget Help entity);

    default HelpSnapshotDTOV1 createSnapshot(Stream<Help> items) {
        return new HelpSnapshotDTOV1()
                .id(UUID.randomUUID().toString())
                .created(OffsetDateTime.now())
                .helps(createHelps(items));
    }

    default Map<String, Map<String, EximHelpDTOV1>> createHelps(Stream<Help> items) {
        if (items == null) {
            return Map.of();
        }
        return items.collect(groupingBy(Help::getProductName, toMap(Help::getItemId, this::createHelp)));
    }

    EximHelpDTOV1 createHelp(Help item);

    default String id(Help help) {
        return id(help.getProductName(), help.getItemId());
    }

    default String id(String productName, String itemId) {
        return productName + "#" + itemId;
    }
}
