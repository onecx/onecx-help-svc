package org.tkit.onecx.help.rs.internal.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.help.rs.internal.model.CreateHelpDTO;
import gen.org.tkit.onecx.help.rs.internal.model.HelpSearchCriteriaDTO;
import gen.org.tkit.onecx.help.rs.internal.model.UpdateHelpDTO;

@ApplicationScoped
public class InternalLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                item(10, CreateHelpDTO.class, x -> {
                    CreateHelpDTO d = (CreateHelpDTO) x;
                    return CreateHelpDTO.class.getSimpleName() + "[" + d.getAppId() + "," + d.getItemId() + "]";
                }),
                item(10, UpdateHelpDTO.class, x -> {
                    UpdateHelpDTO d = (UpdateHelpDTO) x;
                    return UpdateHelpDTO.class.getSimpleName() + "[" + d.getAppId() + "," + d.getItemId() + "]";
                }),
                item(10, HelpSearchCriteriaDTO.class, x -> {
                    HelpSearchCriteriaDTO d = (HelpSearchCriteriaDTO) x;
                    return HelpSearchCriteriaDTO.class.getSimpleName() + "[" + d.getPageNumber() + ","
                            + d.getPageSize()
                            + "]";
                }));
    }
}
