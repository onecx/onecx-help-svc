package org.tkit.onecx.help.domain.di;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.help.domain.daos.HelpDAO;
import org.tkit.onecx.help.domain.di.mappers.DataImportMapperV1;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.context.Context;

import gen.org.tkit.onecx.help.di.v1.model.DataImportHelpDTOV1;

@ApplicationScoped
public class HelmImportService {

    @Inject
    HelpDAO dao;

    @Inject
    DataImportMapperV1 mapper;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void deleteAllCommonData() {
        dao.deleteQueryAll();
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void importTenantHelp(String tenantId, Map<String, DataImportHelpDTOV1> helps) {
        if (helps.isEmpty()) {
            return;
        }
        try {
            var ctx = Context.builder()
                    .principal("data-import")
                    .tenantId(tenantId)
                    .build();
            ApplicationContext.start(ctx);

            helps.forEach((itemId, dto) -> {
                var help = mapper.create(dto, itemId);
                dao.create(help);
            });
        } finally {
            ApplicationContext.close();
        }
    }
}
