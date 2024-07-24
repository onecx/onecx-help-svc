package org.tkit.onecx.help.rs.exim.v1.controllers;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.help.domain.daos.HelpDAO;
import org.tkit.onecx.help.domain.models.Help;
import org.tkit.onecx.help.domain.services.HelpService;
import org.tkit.onecx.help.rs.exim.v1.mappers.HelpEximExceptionMapperV1;
import org.tkit.onecx.help.rs.exim.v1.mappers.HelpEximMapperV1;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.help.rs.exim.v1.HelpExportImportApi;
import gen.org.tkit.onecx.help.rs.exim.v1.model.EximProblemDetailResponseDTOV1;
import gen.org.tkit.onecx.help.rs.exim.v1.model.ExportHelpsRequestDTOV1;
import gen.org.tkit.onecx.help.rs.exim.v1.model.HelpSnapshotDTOV1;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class HelpEximRestControllerV1 implements HelpExportImportApi {

    @Inject
    HelpDAO dao;

    @Inject
    HelpEximMapperV1 mapper;

    @Inject
    HelpEximExceptionMapperV1 exceptionMapper;

    @Inject
    HelpService service;

    @Override
    public Response exportHelpsByProducts(ExportHelpsRequestDTOV1 exportHelpsRequestDTOV1) {
        var helps = dao.findHelpsByProductNames(exportHelpsRequestDTOV1.getProductNames());
        return Response.ok(mapper.createSnapshot(helps)).build();
    }

    @Override
    public Response importOperatorHelps(HelpSnapshotDTOV1 helpSnapshotDTOV1) {

        var productNames = helpSnapshotDTOV1.getHelps().keySet();
        var existingData = dao.findHelpsByProductNames(productNames);
        var map = existingData.collect(toMap(x -> mapper.id(x), x -> x));

        List<Help> create = new ArrayList<>();
        List<Help> update = new ArrayList<>();

        helpSnapshotDTOV1.getHelps().forEach((productName, items) -> {
            items.forEach((itemId, dto) -> {
                var id = mapper.id(productName, itemId);
                var help = map.get(id);
                if (help == null) {

                    create.add(mapper.createHelp(productName, itemId, dto));
                } else {
                    update.add(mapper.updateHelp(dto, help));
                }
            });
        });

        service.importHelp(create, update);
        return Response.ok().build();
    }

    @Override
    public Response importHelps(HelpSnapshotDTOV1 helpSnapshotDTOV1) {
        return importOperatorHelps(helpSnapshotDTOV1);
    }

    @ServerExceptionMapper
    public RestResponse<EximProblemDetailResponseDTOV1> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

}
