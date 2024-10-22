
package io.quarkiverse.jdbc.singlestore.deployment;

import java.time.Duration;
import java.util.Set;

import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy;
import org.testcontainers.shaded.com.google.common.collect.Sets;
import org.testcontainers.utility.DockerImageName;

public class SinglestoreContainer extends JdbcDatabaseContainer<SinglestoreContainer> {
    static final String FULL_IMAGE_NAME = "ghcr.io/singlestore-labs/singlestoredb-dev";
    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse(FULL_IMAGE_NAME);
    static final Integer SINGLESTORE_PORT = 3306;
    private String databaseName;
    private final String rootUsername;
    private final String rootPassword;
    private static final String ROOT_USER = "root";

    public SinglestoreContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public SinglestoreContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        this.rootUsername = ROOT_USER;
        this.rootPassword = ROOT_USER;
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);
        this.addExposedPort(SINGLESTORE_PORT);
    }

    public Set<Integer> getLivenessCheckPortNumbers() {
        return Sets.newHashSet(SINGLESTORE_PORT);
    }

    @Override
    public SinglestoreContainer withDatabaseName(String dbName) {
        this.databaseName = dbName;
        return this;
    }

    protected void configure() {
        if (this.rootPassword != null && !this.rootPassword.isEmpty()) {
            this.addEnv("ROOT_PASSWORD", this.rootPassword);
        } else {
            if (!"root".equalsIgnoreCase(this.rootUsername)) {
                throw new ContainerLaunchException("Empty password can be used only with the root user");
            }
        }
        this.setStartupCheckStrategy(new MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(15)));
        this.setStartupAttempts(3);
    }

    public String getDriverClassName() {
        return "com.singlestore.jdbc.Driver";
    }

    public String getJdbcUrl() {
        String additionalUrlParams = this.constructUrlParameters("?", "&");
        return "jdbc:singlestore://" + this.getHost() + ":" + this.getMappedPort(SINGLESTORE_PORT) + "/" + getDatabaseName()
                + additionalUrlParams;
    }

    public String getUsername() {
        return this.rootUsername;
    }

    public String getPassword() {
        return this.rootPassword;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    public String getTestQueryString() {
        return "SELECT 1";
    }
}
