package io.github.onecx.help.rs.v1.controllers;

import static jakarta.transaction.Transactional.TxType.NOT_SUPPORTED;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.io.github.onecx.help.rs.v1.HelpsV1Api;
import gen.io.github.onecx.help.rs.v1.model.HelpSearchCriteriaDTOV1;
import gen.io.github.onecx.help.rs.v1.model.ProblemDetailResponseDTOV1;
import io.github.onecx.help.domain.daos.HelpDAO;
import io.github.onecx.help.rs.v1.mappers.CriteriaMapper;
import io.github.onecx.help.rs.v1.mappers.ExceptionMapper;
import io.github.onecx.help.rs.v1.mappers.HelpMapper;

@LogService
@ApplicationScoped
@Transactional(value = NOT_SUPPORTED)
public class HelpsV1RestController implements HelpsV1Api {
    @Inject
    HelpDAO dao;

    @Inject
    CriteriaMapper mapper;

    @Inject
    HelpMapper helpMapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response searchHelpItem(HelpSearchCriteriaDTOV1 helpSearchCriteriaDTOV1) {
        var help = dao.findByAppIdAndItemId(mapper.map(helpSearchCriteriaDTOV1));
        return Response.ok(helpMapper.map(help)).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTOV1> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTOV1> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
