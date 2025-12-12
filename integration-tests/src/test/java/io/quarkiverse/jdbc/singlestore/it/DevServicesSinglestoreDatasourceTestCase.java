package io.quarkiverse.jdbc.singlestore.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.concurrent.atomic.AtomicReference;
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
    public void testDatasource() {
        final AtomicReference<AgroalConnectionPoolConfiguration> reference = new AtomicReference<>();
        assertThatCode(() -> reference.getAndSet(dataSource.getConfiguration().connectionPoolConfiguration()))
                .doesNotThrowAnyException();
        AgroalConnectionPoolConfiguration configuration = reference.get();
        assertThat(configuration.connectionFactoryConfiguration().jdbcUrl()).contains("jdbc:singlestore:");
        assertThat(configuration.connectionFactoryConfiguration().principal().getName()).isEqualTo("quarkus");
        assertThat(configuration.maxSize()).isIn(20, 50);// Complies with latest snapshot i.e 20 -> 50
        assertThat(configuration.exceptionSorter()).isInstanceOf(MySQLExceptionSorter.class);
        assertThatCode(() -> dataSource.getConnection().close()).doesNotThrowAnyException();
    }
}
