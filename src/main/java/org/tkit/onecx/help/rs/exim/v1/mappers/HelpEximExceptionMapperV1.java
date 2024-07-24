package org.tkit.onecx.help.rs.exim.v1.mappers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.log.cdi.LogService;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.help.rs.exim.v1.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface HelpEximExceptionMapperV1 {

    @LogService(log = false)
    default RestResponse<EximProblemDetailResponseDTOV1> constraint(ConstraintViolationException ex) {
        var dto = exception(ErrorKeys.CONSTRAINT_VIOLATIONS.name(), ex.getMessage());
        dto.setInvalidParams(createErrorValidationResponse(ex.getConstraintViolations()));
        return RestResponse.status(Response.Status.BAD_REQUEST, dto);
    }

    default List<EximProblemDetailParamDTOV1> map(Map<String, Object> params) {
        if (params == null) {
            return List.of();
        }
        return params.entrySet().stream().map(e -> {
            var item = new EximProblemDetailParamDTOV1();
            item.setKey(e.getKey());
            if (e.getValue() != null) {
                item.setValue(e.getValue().toString());
            }
            return item;
        }).toList();
    }

    @Mapping(target = "invalidParams", ignore = true)
    @Mapping(target = "removeInvalidParamsItem", ignore = true)
    @Mapping(target = "removeParamsItem", ignore = true)
    @Mapping(target = "params", ignore = true)
    EximProblemDetailResponseDTOV1 exception(String errorCode, String detail);

    List<EximProblemDetailInvalidParamDTOV1> createErrorValidationResponse(
            Set<ConstraintViolation<?>> constraintViolation);

    @Mapping(target = "name", source = "propertyPath")
    @Mapping(target = "message", source = "message")
    EximProblemDetailInvalidParamDTOV1 createError(ConstraintViolation<?> constraintViolation);

    default String mapPath(Path path) {
        return path.toString();
    }

    enum ErrorKeys {

        CONSTRAINT_VIOLATIONS;
    }
}
