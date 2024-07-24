package org.tkit.onecx.help.rs.exim.v1.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.help.rs.exim.v1.model.ExportHelpsRequestDTOV1;
import gen.org.tkit.onecx.help.rs.exim.v1.model.HelpSnapshotDTOV1;

@ApplicationScoped
public class HelpEximLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                item(10, ExportHelpsRequestDTOV1.class, x -> x.getClass().getSimpleName()),
                item(10, HelpSnapshotDTOV1.class,
                        x -> x.getClass().getSimpleName() + ":" + ((HelpSnapshotDTOV1) x).getId()));
    }
}
