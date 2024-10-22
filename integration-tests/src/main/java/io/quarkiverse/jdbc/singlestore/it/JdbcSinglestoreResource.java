/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkiverse.jdbc.singlestore.it;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.agroal.api.AgroalDataSource;

@Path("/jdbc-singlestore")
@ApplicationScoped
public class JdbcSinglestoreResource {

    @Inject
    AgroalDataSource ds;

    @ConfigProperty(name = "quarkus.datasource.jdbc.url")
    String jdbcUrl;

    @ConfigProperty(name = "quarkus.datasource.username")
    String user;

    @ConfigProperty(name = "quarkus.datasource.password")
    String password;

    @GET
    @Path("/agroal")
    public String testSinglstoreAgroal() throws SQLException {

        String result;
        try (Connection connection = ds.getConnection()) {
            System.out.println("connection = " + connection.getClass().getName() + ":" + connection);
            DatabaseMetaData metaData = connection.getMetaData();

            System.out.println("conn conn=" + metaData.getConnection());
            System.out.println("conn url=" + metaData.getURL());
            result = test(connection);
        }
        return result;
    }

    @GET
    @Path("/connection")
    public String connection() throws SQLException {
        String result;
        try (Connection connection = DriverManager.getConnection(jdbcUrl + "?user=" + user + "&password=" + password)) {
            result = test(connection);
        }
        return result;
    }

    private String test(Connection connection) throws SQLException {
        StringBuilder result = new StringBuilder();
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            statement.executeUpdate("drop table if exists defaultreason");
            statement.executeUpdate(
                    """
                            CREATE TABLE `defaultreason` (
                              `id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
                              `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
                              PRIMARY KEY (`id`),
                              SHARD KEY `__SHARDKEY` (`id`),
                              SORT KEY `__UNORDERED` ()
                            ) AUTOSTATS_CARDINALITY_MODE=INCREMENTAL AUTOSTATS_HISTOGRAM_MODE=CREATE AUTOSTATS_SAMPLING=ON SQL_MODE='STRICT_ALL_TABLES';""");
            statement.executeUpdate("insert into defaultreason values('1', 'leo')");
            try (ResultSet rs = statement.executeQuery("select * from defaultreason where id = '1'")) {
                while (rs.next()) {
                    result.append(rs.getString("id")).append("/").append(rs.getString("description")).append("/");
                }
            }
        }
        return result.toString();
    }
}
