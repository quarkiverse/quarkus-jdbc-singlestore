package io.quarkiverse.jdbc.singlestore.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.AgroalConnectionPoolConfiguration;
import io.agroal.api.exceptionsorter.MySQLExceptionSorter;
import io.quarkus.test.QuarkusUnitTest;

public class DevServicesSinglestoreDatasourceTestCase {

    @RegisterExtension
    static QuarkusUnitTest test = new QuarkusUnitTest()
            .withEmptyApplication()
            //            .overrideRuntimeConfigKey("singlestore.image","test")
            //            .overrideRuntimeConfigKey("quarkus.datasource.db-kind","singlestore")
            // Expect no warnings (in particular from Agroal)
            .setLogRecordPredicate(record -> record.getLevel().intValue() >= Level.WARNING.intValue()
                    // There are other warnings: JDK8, TestContainers, drivers, ...
                    // Ignore them: we're only interested in Agroal here.
                    && record.getMessage().contains("Agroal"))
            .assertLogRecords(records -> Assertions.assertThat(records)
                    // This is just to get meaningful error messages, as LogRecord doesn't have a toString()
                    .extracting(LogRecord::getMessage)
                    .isEmpty());

    @Inject
    AgroalDataSource dataSource;

    @Test
    public void testDatasource() throws Exception {
        AgroalConnectionPoolConfiguration configuration = null;

        try {
            configuration = dataSource.getConfiguration().connectionPoolConfiguration();
        } catch (NullPointerException e) {
            // we catch the NPE here as we have a proxycd and we can't test dataSource directly
            fail("Datasource should not be null");
        }
        assertTrue(configuration.connectionFactoryConfiguration().jdbcUrl().contains("jdbc:singlestore:"));
        assertEquals("quarkus", configuration.connectionFactoryConfiguration().principal().getName());
        assertEquals(20, configuration.maxSize());
        assertThat(configuration.exceptionSorter()).isInstanceOf(MySQLExceptionSorter.class);

        try (Connection connection = dataSource.getConnection()) {
        }
    }
}