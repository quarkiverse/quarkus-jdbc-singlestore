package io.quarkiverse.jdbc.singlestore.deployment;

import static io.quarkiverse.jdbc.singlestore.runtime.SinglestoreConstants.DB_KIND;

import org.hibernate.community.dialect.MariaDBLegacyDialect;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.hibernate.orm.deployment.spi.DatabaseKindDialectBuildItem;

public class SinglestoreHibernateProcessor {

    @BuildStep
    void processHibernate(
            BuildProducer<DatabaseKindDialectBuildItem> producer) {
        producer.produce(
                DatabaseKindDialectBuildItem.forThirdPartyDialect(DB_KIND,
                        MariaDBLegacyDialect.class.getName()));
        //        producer.produce(
        //                new DatabaseKindDialectBuildItem(DB_KIND,
        //                        MariaDBLegacyDialect.class.getName()));
    }
}
