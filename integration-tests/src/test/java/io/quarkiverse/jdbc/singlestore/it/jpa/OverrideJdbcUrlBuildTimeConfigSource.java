package io.quarkiverse.jdbc.singlestore.it.jpa;

import java.util.HashMap;

import jakarta.annotation.Priority;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;

import io.smallrye.config.Priorities;
import io.smallrye.config.common.MapBackedConfigSource;

// To test https://github.com/quarkusio/quarkus/issues/16123
@Priority(Priorities.APPLICATION + 100)
public class OverrideJdbcUrlBuildTimeConfigSource extends MapBackedConfigSource {
    public OverrideJdbcUrlBuildTimeConfigSource() {
        super(OverrideJdbcUrlBuildTimeConfigSource.class.getName(), new HashMap<>(), 1000);
    }

    @Override
    public String getValue(final String propertyName) {
        if (!propertyName.equals("quarkus.datasource.jdbc.url")) {
            return super.getValue(propertyName);
        }

        boolean isBuildTime = false;
        for (ConfigSource configSource : ConfigProvider.getConfig().getConfigSources()) {
            if (configSource.getClass().getSimpleName().equals("BuildTimeEnvConfigSource")) {
                isBuildTime = true;
                break;
            }
        }

        if (isBuildTime) {
            return "${singlestore.url}";
        }

        return super.getValue(propertyName);
    }
}
