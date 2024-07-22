package org.tkit.onecx.help.rs.internal.controllers;

import static jakarta.transaction.Transactional.TxType.NOT_SUPPORTED;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.help.domain.daos.ProductResourceDAO;
import org.tkit.onecx.help.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.help.rs.internal.mappers.ProductResourceMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.help.rs.internal.ProductResourceInternalApi;
import gen.org.tkit.onecx.help.rs.internal.model.CreateProductResourceDTO;
import gen.org.tkit.onecx.help.rs.internal.model.ProblemDetailResponseDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LogService
@ApplicationScoped
@Transactional(value = NOT_SUPPORTED)
public class ProductResourcesRestController implements ProductResourceInternalApi {

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    ProductResourceMapper mapper;

    @Inject
    ProductResourceDAO dao;

    @Context
    UriInfo uriInfo;

    @Override
    public Response createProductResource(CreateProductResourceDTO createProductResourceDTO) {
        var item = mapper.create(createProductResourceDTO);
        item = dao.create(item);

        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(item.getId()).build())
                .entity(mapper.map(item))
                .build();
    }

    @Override
    public Response deleteProductResource(String id) {
        dao.deleteQueryById(id);
        return Response.noContent().build();
    }

    @Override
    public Response getAllProductResources() {
        return Response.ok(mapper.map(dao.findAll())).build();
    }

    @Override
    public Response getProductResource(String id) {
        var item = dao.findById(id);
        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.map(item)).build();
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
