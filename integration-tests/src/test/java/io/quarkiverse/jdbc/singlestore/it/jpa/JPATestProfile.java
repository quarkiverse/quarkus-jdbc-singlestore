package io.quarkiverse.jdbc.singlestore.it.jpa;

import java.util.Collections;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class JPATestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Collections.singletonMap("quarkus.hibernate-orm.database.generation", "drop-and-create");
    }
}
