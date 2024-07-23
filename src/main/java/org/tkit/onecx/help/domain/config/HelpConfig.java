package org.tkit.onecx.help.domain.config;

import io.quarkus.runtime.annotations.ConfigDocFilename;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigDocFilename("onecx-help-svc.adoc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "onecx.help")
public interface HelpConfig {

    /**
     * Enable or disable default help
     */
    @WithName("default.enabled")
    @WithDefault("false")
    boolean defaultHelpEnabled();

    /**
     * Default help URL
     */
    @WithName("default.url")
    @WithDefault("https://github.com/onecx")
    String defaultHelpUrl();

    /**
     * Default help URL
     */
    @WithName("product-item-id")
    @WithDefault("PRODUCT_BASE_DOC_URL")
    String productItemId();
}
