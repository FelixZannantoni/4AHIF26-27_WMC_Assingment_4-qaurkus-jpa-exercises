package at.htl.catalogue;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "catalogue")
public interface CatalogueConfig {

    @WithDefault("50")
    int maxResults();

    @WithDefault("false")
    boolean audit();
}
