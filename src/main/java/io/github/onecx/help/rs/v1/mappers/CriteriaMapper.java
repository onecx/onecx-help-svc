package io.github.onecx.help.rs.v1.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import gen.io.github.onecx.help.rs.v1.model.HelpSearchCriteriaDTOV1;
import io.github.onecx.help.domain.criteria.HelpSearchCriteria;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CriteriaMapper {
    HelpSearchCriteria map(HelpSearchCriteriaDTOV1 criteriaDTO);
}
