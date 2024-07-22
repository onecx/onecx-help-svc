package org.tkit.onecx.help.domain.di;

import static org.tkit.onecx.help.domain.di.mappers.TemplateImportMapper.prId;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.help.domain.daos.HelpDAO;
import org.tkit.onecx.help.domain.daos.ProductResourceDAO;
import org.tkit.onecx.help.domain.di.mappers.TemplateImportMapper;
import org.tkit.onecx.help.domain.models.Help;
import org.tkit.onecx.help.domain.models.ProductResource;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.context.Context;

@ApplicationScoped
public class HelpImportService {

    private static final String PRINCIPAL = "template-import";

    @Inject
    HelpDAO dao;

    @Inject
    ProductResourceDAO productResourceDAO;

    public Map<String, ProductResource> getExistingProductResources() {
        return productResourceDAO.findAll().collect(Collectors.toMap(TemplateImportMapper::prId, x -> x));
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Map<String, ProductResource> createProductResources(List<ProductResource> createData) {

        try {
            var ctx = Context.builder()
                    .principal(PRINCIPAL)
                    .build();
            ApplicationContext.start(ctx);

            return productResourceDAO.create(createData).collect(Collectors.toMap(TemplateImportMapper::prId, x -> x));

        } finally {
            ApplicationContext.close();
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void importHelp(String tenant, List<Help> helps) {
        try {
            var ctx = Context.builder()
                    .principal(PRINCIPAL)
                    .tenantId(tenant)
                    .build();

            ApplicationContext.start(ctx);

            // create themes
            dao.create(helps);

        } finally {
            ApplicationContext.close();
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Map<String, Help> getData(String tenant) {
        try {
            var ctx = Context.builder()
                    .principal("data-import")
                    .tenantId(tenant)
                    .build();

            ApplicationContext.start(ctx);
            return dao.loadAll().collect(Collectors.toMap(x -> prId(x.getProductResource()), x -> x));

        } finally {
            ApplicationContext.close();
        }
    }

}
