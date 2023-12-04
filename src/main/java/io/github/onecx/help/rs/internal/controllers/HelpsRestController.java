package io.github.onecx.help.rs.internal.controllers;

import static jakarta.transaction.Transactional.TxType.NOT_SUPPORTED;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;

import gen.io.github.onecx.help.rs.internal.HelpsInternalApi;
import gen.io.github.onecx.help.rs.internal.model.*;
import io.github.onecx.help.domain.daos.HelpDAO;
import io.github.onecx.help.rs.internal.mappers.ExceptionMapper;
import io.github.onecx.help.rs.internal.mappers.HelpMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/internal/helps") // remove this after quarkus fix ServiceExceptionMapper for impl classes
@ApplicationScoped
@Transactional(value = NOT_SUPPORTED)
public class HelpsRestController implements HelpsInternalApi {

    @Inject
    HelpDAO dao;

    @Inject
    HelpMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Context
    UriInfo uriInfo;

    @Override
    @Transactional
    public Response createNewHelp(CreateHelpDTO createHelpDTO) {
        var help = mapper.create(createHelpDTO);
        help = dao.create(help);
        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(help.getId()).build())
                .entity(mapper.map(help))
                .build();
    }

    @Override
    @Transactional
    public Response deleteHelp(String id) {
        dao.deleteQueryById(id);
        return Response.noContent().build();
    }

    @Override
    public Response getHelpById(String id) {
        var help = dao.findById(id);
        if (help == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.map(help)).build();
    }

    @Override
    public Response getHelpByItemId(String itemId) {
        var help = dao.findHelpByItemId(itemId);
        if (help == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.map(help)).build();
    }

    @Override
    public Response getHelps(Integer pageNumber, Integer pageSize) {
        var items = dao.findAll(pageNumber, pageSize);
        return Response.ok(mapper.mapPage(items)).build();
    }

    @Override
    public Response searchHelps(HelpSearchCriteriaDTO helpSearchCriteriaDTO) {
        var criteria = mapper.map(helpSearchCriteriaDTO);
        var result = dao.findHelpsByCriteria(criteria);
        return Response.ok(mapper.mapPage(result)).build();
    }

    @Override
    @Transactional
    public Response updateHelp(String id, UpdateHelpDTO updateHelpDTO) {

        var help = dao.findById(id);
        if (help == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        mapper.update(updateHelpDTO, help);
        dao.update(help);
        return Response.noContent().build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
