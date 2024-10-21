package io.quarkiverse.jdbc.singlestore.deployment;

import static io.quarkus.datasource.deployment.spi.DatabaseDefaultSetupConfig.DEFAULT_DATABASE_NAME;
import static io.quarkus.datasource.deployment.spi.DatabaseDefaultSetupConfig.DEFAULT_DATABASE_PASSWORD;
import static io.quarkus.datasource.deployment.spi.DatabaseDefaultSetupConfig.DEFAULT_DATABASE_USERNAME;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import org.jboss.logging.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.utility.DockerImageName;

import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.HostConfig;

import io.quarkiverse.jdbc.singlestore.runtime.SinglestoreConstants;
import io.quarkus.datasource.common.runtime.DataSourceUtil;
import io.quarkus.datasource.deployment.spi.DevServicesDatasourceContainerConfig;
import io.quarkus.datasource.deployment.spi.DevServicesDatasourceProvider;
import io.quarkus.datasource.deployment.spi.DevServicesDatasourceProviderBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerShutdownCloseable;
import io.quarkus.devservices.common.Labels;
import io.quarkus.devservices.common.Volumes;
import io.quarkus.runtime.LaunchMode;

public class SinglestoreDevServicesProcessor {

    private static final Logger LOG = Logger.getLogger(SinglestoreDevServicesProcessor.class);

    public static final Integer PORT = 3306;

    @BuildStep
    DevServicesDatasourceProviderBuildItem setupSinglestore(
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            GlobalDevServicesConfig globalDevServicesConfig) {
        return new DevServicesDatasourceProviderBuildItem(SinglestoreConstants.DB_KIND, new DevServicesDatasourceProvider() {
            @Override
            public RunningDevServicesDatasource startDatabase(Optional<String> username, Optional<String> password,
                    String datasourceName, DevServicesDatasourceContainerConfig containerConfig,
                    LaunchMode launchMode, Optional<Duration> startupTimeout) {

                boolean useSharedNetwork = DevServicesSharedNetworkBuildItem.isSharedNetworkRequired(globalDevServicesConfig,
                        devServicesSharedNetworkBuildItem);
                //                boolean useSharedNetwork = false;
                QuarkusSinglestoreContainer container = new QuarkusSinglestoreContainer(containerConfig.getImageName(),
                        containerConfig.getFixedExposedPort(),
                        useSharedNetwork);
                startupTimeout.ifPresent(container::withStartupTimeout);

                String effectiveUsername = containerConfig.getUsername().orElse(username.orElse(DEFAULT_DATABASE_USERNAME));
                String effectivePassword = containerConfig.getPassword().orElse(password.orElse(DEFAULT_DATABASE_PASSWORD));
                String effectiveDbName = containerConfig.getDbName().orElse(
                        DataSourceUtil.isDefault(datasourceName) ? DEFAULT_DATABASE_NAME : datasourceName);

                try {
                    Path dbUserSql = Files.createTempFile("init-", ".sql");
                    // Writing data here
                    String dbUserScript = String.format("""
                            CREATE DATABASE IF NOT EXISTS '%s';
                            CREATE USER %s@'%%' identified by '%s';
                            GRANT ALL ON * to '%s';""", effectiveDbName, effectiveUsername, effectivePassword,
                            effectiveUsername);
                    byte[] buf = dbUserScript.getBytes();
                    Files.write(dbUserSql, buf);
                    File dbUserSqlFile = dbUserSql.toFile();
                    dbUserSqlFile.deleteOnExit();
                    container.withCreateContainerCmdModifier(it -> HostConfig.newHostConfig().withCapAdd(Capability.SYS_ADMIN)
                            .withSecurityOpts(
                                    List.of("apparmor:unconfined", "label=disable", "seccomp=unconfined", "unmask=all")));
                    container.withFileSystemBind(dbUserSqlFile.getAbsolutePath(), "/init.sql", BindMode.READ_ONLY);
                    //                    container.withPrivilegedMode(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                container
                        .withReuse(containerConfig.isReuse());
                Labels.addDataSourceLabel(container, datasourceName);
                Volumes.addVolumes(container, containerConfig.getVolumes());

                container.withEnv(containerConfig.getContainerEnv());

                containerConfig.getAdditionalJdbcUrlProperties().forEach(container::withUrlParam);
                containerConfig.getCommand().ifPresent(container::setCommand);
                containerConfig.getInitScriptPath().ifPresent(container::withInitScript);
                container.withDatabaseName("");// bypass for root user
                container.start();
                await().until(container::isHealthy);

                // Set the real dbNAme, once user is created.
                container
                        .withDatabaseName(effectiveDbName);
                LOG.info("Dev Services for Singlestore started.");

                return new RunningDevServicesDatasource(container.getContainerId(),
                        container.getEffectiveJdbcUrl(),
                        container.getReactiveUrl(),
                        effectiveUsername,
                        effectivePassword,
                        new ContainerShutdownCloseable(container, "Singlestore"));
            }
        });
    }

    private static class QuarkusSinglestoreContainer extends SinglestoreContainer {
        private final OptionalInt fixedExposedPort;
        private final boolean useSharedNetwork;

        private String hostName = null;

        public QuarkusSinglestoreContainer(Optional<String> imageName, OptionalInt fixedExposedPort, boolean useSharedNetwork) {
            super(DockerImageName
                    .parse(imageName.orElseGet(() -> ConfigureUtil.getDefaultImageNameFor("singlestore")))
                    .asCompatibleSubstituteFor(
                            DockerImageName.parse(SinglestoreContainer.FULL_IMAGE_NAME)));
            this.fixedExposedPort = fixedExposedPort;
            this.useSharedNetwork = useSharedNetwork;
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
        public String getEffectiveJdbcUrl() {
            if (useSharedNetwork) {
                String additionalUrlParams = constructUrlParameters("?", "&");
                return "jdbc:singlestore://" + hostName + ":" + PORT + "/" + getDatabaseName() + additionalUrlParams;
            } else {
                return super.getJdbcUrl();
            }
        }

        public String getReactiveUrl() {
            return getEffectiveJdbcUrl().replaceFirst("jdbc:singlestore:", "vertx-reactive:singlestore:");
        }
    }
}
