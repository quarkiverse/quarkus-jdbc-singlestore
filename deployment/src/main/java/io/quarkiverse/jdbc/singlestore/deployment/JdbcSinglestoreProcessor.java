package io.quarkiverse.jdbc.singlestore.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class JdbcSinglestoreProcessor {

    private static final String FEATURE = "jdbc-singlestore";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
