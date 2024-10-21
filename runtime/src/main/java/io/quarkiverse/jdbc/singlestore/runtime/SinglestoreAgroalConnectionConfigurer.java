package io.quarkiverse.jdbc.singlestore.runtime;

import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.exceptionsorter.MySQLExceptionSorter;
import io.quarkus.agroal.runtime.AgroalConnectionConfigurer;
import io.quarkus.agroal.runtime.JdbcDriver;

@JdbcDriver(SinglestoreConstants.DB_KIND)
public class SinglestoreAgroalConnectionConfigurer implements AgroalConnectionConfigurer {

    @Override
    public void disableSslSupport(String databaseKind, AgroalDataSourceConfigurationSupplier dataSourceConfiguration) {
        dataSourceConfiguration.connectionPoolConfiguration().connectionFactoryConfiguration().jdbcProperty("useSSL", "false");
    }

    @Override
    public void setExceptionSorter(String databaseKind, AgroalDataSourceConfigurationSupplier dataSourceConfiguration) {
        // This exception sorter is apparently valid for Singlestore too.
        dataSourceConfiguration.connectionPoolConfiguration().exceptionSorter(new MySQLExceptionSorter());
    }

}
