package io.quarkiverse.jdbc.singlestore.deployment;

import static io.quarkus.datasource.deployment.spi.DatabaseDefaultSetupConfig.DEFAULT_DATABASE_NAME;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;

import org.jboss.logging.Logger;
import org.testcontainers.utility.DockerImageName;

import io.quarkiverse.jdbc.singlestore.runtime.SinglestoreConstants;
import io.quarkus.datasource.common.runtime.DataSourceUtil;
import io.quarkus.datasource.deployment.spi.DatasourceStartable;
import io.quarkus.datasource.deployment.spi.DevServicesDatasourceContainerConfig;
import io.quarkus.datasource.deployment.spi.DevServicesDatasourceProvider;
import io.quarkus.datasource.deployment.spi.DevServicesDatasourceProviderBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesComposeProjectBuildItem;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.Labels;
import io.quarkus.devservices.common.Volumes;
import io.quarkus.runtime.LaunchMode;

public class SinglestoreDevServicesProcessor {

    private static final Logger LOG = Logger.getLogger(SinglestoreDevServicesProcessor.class);

    public static final Integer PORT = 3306;

    @BuildStep
    DevServicesDatasourceProviderBuildItem setupSinglestore() {

        return new DevServicesDatasourceProviderBuildItem(SinglestoreConstants.DB_KIND, new DevServicesDatasourceProvider() {
            @Override
            public String getFeature() {
                return "jdbc-singlestore";
            }

            @Override
            public DatasourceStartable createDatasourceStartable(Optional<String> username, Optional<String> password,
                    String datasourceName, DevServicesDatasourceContainerConfig containerConfig,
                    LaunchMode launchMode, boolean useSharedNetwork, Optional<Duration> startupTimeout) {

                String effectiveDbName = containerConfig.getDbName().orElse(
                        DataSourceUtil.isDefault(datasourceName) ? DEFAULT_DATABASE_NAME : datasourceName);

                QuarkusSinglestoreContainer container = new QuarkusSinglestoreContainer(containerConfig.getImageName(),
                        containerConfig.getFixedExposedPort(), effectiveDbName,
                        useSharedNetwork);
                startupTimeout.ifPresent(container::withStartupTimeout);

                container
                        .withReuse(containerConfig.isReuse());
                Labels.addDataSourceLabel(container, datasourceName);
                Volumes.addVolumes(container, containerConfig.getVolumes());

                container.withEnv(containerConfig.getContainerEnv());

                containerConfig.getAdditionalJdbcUrlProperties().forEach(container::withUrlParam);
                containerConfig.getCommand().ifPresent(container::setCommand);
                containerConfig.getInitScriptPath().ifPresent(container::withInitScripts);
                container.withDatabaseName("");// bypass for root user

                return container;
            }

            @Override
            public Optional<DevServicesDatasourceProvider.RunningDevServicesDatasource> findRunningComposeDatasource(
                    LaunchMode launchMode, boolean useSharedNetwork, DevServicesDatasourceContainerConfig containerConfig,
                    DevServicesComposeProjectBuildItem composeProjectBuildItem) {
                // No compose support
                return Optional.empty();
            }
        });
    }

    private static class QuarkusSinglestoreContainer extends SinglestoreContainer implements DatasourceStartable {
        private final OptionalInt fixedExposedPort;
        private final boolean useSharedNetwork;

        private String hostName = null;
        private final String effectiveDbName;

        public QuarkusSinglestoreContainer(Optional<String> imageName, OptionalInt fixedExposedPort, String effectiveDbName,
                boolean useSharedNetwork) {
            super(DockerImageName
                    .parse(imageName.orElseGet(() -> ConfigureUtil.getDefaultImageNameFor("singlestore")))
                    .asCompatibleSubstituteFor(
                            DockerImageName.parse(SinglestoreContainer.FULL_IMAGE_NAME)));
            this.fixedExposedPort = fixedExposedPort;
            this.useSharedNetwork = useSharedNetwork;
            this.effectiveDbName = effectiveDbName;
        }

        @Override
        public void start() {
            super.start();
            await().until(this::isHealthy);

            try (Connection connection = this.createConnection("");
                    Statement statement = connection.createStatement()) {
                //                    container.execInContainer("singlestore", "-p${ROOT_PASSWORD}" ,"<", "/user.sql");
                connection.setAutoCommit(false);
                statement.addBatch(String.format("CREATE DATABASE IF NOT EXISTS '%s';", effectiveDbName));//inserting Query in stmt
                statement.addBatch(
                        String.format("CREATE USER %s@'%%' identified by '%s';", getUsername(), getPassword()));
                statement.addBatch(String.format("GRANT ALL ON * to '%s';", getUsername()));
                statement.executeBatch();
                LOG.info("User is created.");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // Set the real dbNaAme, once user is created.
            this.withDatabaseName(effectiveDbName);
        }

        @Override
        public SinglestoreContainer withDatabaseName(String dbName) {
            return super.withDatabaseName(dbName);
        }

        @Override
        public SinglestoreContainer withReuse(boolean reusable) {
            return super.withReuse(reusable);
        }

        @Override
        protected void configure() {
            super.configure();

            if (useSharedNetwork) {
                hostName = ConfigureUtil.configureSharedNetwork(this, "singlestore");
                return;
            }

            if (fixedExposedPort.isPresent()) {
                addFixedExposedPort(fixedExposedPort.getAsInt(), PORT);
            } else {
                addExposedPort(PORT);
            }
        }

        // this is meant to be called by Quarkus code and is needed in order to not disrupt testcontainers
        // from being able to determine the status of the container (which it does by trying to acquire a connection)
        @Override
        public String getEffectiveJdbcUrl() {
            if (useSharedNetwork) {
                String additionalUrlParams = constructUrlParameters("?", "&");
                return "jdbc:singlestore://" + hostName + ":" + PORT + "/" + getDatabaseName() + additionalUrlParams;
            } else {
                return super.getJdbcUrl();
            }
        }

        @Override
        public String getReactiveUrl() {
            return getEffectiveJdbcUrl().replaceFirst("jdbc:singlestore:", "vertx-reactive:singlestore:");
        }

        @Override
        public void close() {
            super.close();
        }

        @Override
        public String getConnectionInfo() {
            return getEffectiveJdbcUrl();
        }
    }
}
