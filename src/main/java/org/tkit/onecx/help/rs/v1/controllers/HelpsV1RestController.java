package org.tkit.onecx.help.rs.v1.controllers;

import static jakarta.transaction.Transactional.TxType.NOT_SUPPORTED;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.help.domain.daos.HelpDAO;
import org.tkit.onecx.help.rs.v1.mappers.ExternalV1Mapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.help.rs.v1.HelpsV1Api;

@LogService
@ApplicationScoped
@Transactional(value = NOT_SUPPORTED)
public class HelpsV1RestController implements HelpsV1Api {
    @Inject
    HelpDAO dao;

    @Inject
    ExternalV1Mapper mapper;

    @Override
    public Response searchHelpItem(String productName, String helpItemId) {
        var help = dao.findByProductNameAndItemId(productName, helpItemId);
        if (help == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.map(help)).build();
    }
}
