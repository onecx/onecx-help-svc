package org.tkit.onecx.help.rs.internal.controllers;

import static jakarta.transaction.Transactional.TxType.NOT_SUPPORTED;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.help.domain.daos.HelpDAO;
import org.tkit.onecx.help.domain.daos.ProductResourceDAO;
import org.tkit.onecx.help.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.help.rs.internal.mappers.HelpMapper;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.help.rs.internal.HelpsInternalApi;
import gen.org.tkit.onecx.help.rs.internal.model.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LogService
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

    @Inject
    ProductResourceDAO productResourceDAO;

    @Override
    public Response createNewHelp(CreateHelpDTO createHelpDTO) {
        var pr = productResourceDAO.findByProductNameAndItemId(createHelpDTO.getProductName(), createHelpDTO.getItemId());
        if (pr == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var help = mapper.create(createHelpDTO);
        help.setProductResource(pr);
        help = dao.create(help);

        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(help.getId()).build())
                .entity(mapper.map(help))
                .build();
    }

    @Override
    public Response deleteHelp(String id) {
        dao.deleteQueryById(id);
        return Response.noContent().build();
    }

    @Override
    public Response getAllProductsWithHelpItems() {
        var productNames = productResourceDAO.findAllProductNames();
        var result = mapper.map(productNames);
        return Response.ok(result).build();
    }

    @Override
    public Response getHelpById(String id) {
        var help = dao.loadById(id);
        if (help == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.map(help)).build();
    }

    @Override
    public Response searchHelps(HelpSearchCriteriaDTO helpSearchCriteriaDTO) {
        var criteria = mapper.map(helpSearchCriteriaDTO);

        var items = dao.findHelpsByCriteria(criteria);
        var list = items.getStream().toList();

        var result = new PageResult<>(items.getTotalElements(), list.stream(),
                Page.of(criteria.getPageNumber(), criteria.getPageSize()));

        return Response.ok(mapper.mapPage(result)).build();
    }

    @Override
    public Response updateHelp(String id, UpdateHelpDTO updateHelpDTO) {
        var help = dao.findById(id);
        if (help == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        mapper.update(updateHelpDTO, help);

        var pr = productResourceDAO.findByProductNameAndItemId(updateHelpDTO.getProductName(), updateHelpDTO.getItemId());
        if (pr == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        help.setProductResource(pr);

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

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> optimisticLockException(OptimisticLockException ex) {
        return exceptionMapper.optimisticLock(ex);
    }
}
