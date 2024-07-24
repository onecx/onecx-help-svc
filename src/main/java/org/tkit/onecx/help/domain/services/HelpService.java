package org.tkit.onecx.help.domain.services;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.help.domain.daos.HelpDAO;
import org.tkit.onecx.help.domain.models.Help;

@ApplicationScoped
public class HelpService {

    @Inject
    HelpDAO dao;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void importHelp(List<Help> create, List<Help> update) {
        dao.update(update);
        dao.create(create);
    }
}
