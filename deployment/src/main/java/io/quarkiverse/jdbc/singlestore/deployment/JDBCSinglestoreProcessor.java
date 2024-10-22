package io.quarkiverse.jdbc.singlestore.deployment;

import static io.quarkiverse.jdbc.singlestore.runtime.SinglestoreConstants.DB_KIND;

import io.quarkiverse.jdbc.singlestore.runtime.SinglestoreAgroalConnectionConfigurer;
import io.quarkiverse.jdbc.singlestore.runtime.SinglestoreServiceBindingConverter;
import io.quarkus.agroal.spi.JdbcDriverBuildItem;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.datasource.deployment.spi.DefaultDataSourceDbKindBuildItem;
import io.quarkus.datasource.deployment.spi.DevServicesDatasourceConfigurationHandlerBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;
import io.quarkus.deployment.pkg.steps.NativeOrNativeSourcesBuild;

public class JDBCSinglestoreProcessor {

    private static final String FEATURE = "jdbc-singlestore";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerDriver(BuildProducer<JdbcDriverBuildItem> jdbcDriver, BuildProducer<DefaultDataSourceDbKindBuildItem> dbKind) {
        jdbcDriver.produce(
                new JdbcDriverBuildItem(DB_KIND, "com.singlestore.jdbc.Driver",
                        "com.singlestore.jdbc.SingleStoreDataSource"));

        dbKind.produce(new DefaultDataSourceDbKindBuildItem(DB_KIND));
    }

    @BuildStep
    DevServicesDatasourceConfigurationHandlerBuildItem devDbHandler() {
        return DevServicesDatasourceConfigurationHandlerBuildItem.jdbc(DB_KIND);
    }

    @BuildStep
    void configureAgroalConnection(BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            Capabilities capabilities) {
        if (capabilities.isPresent(Capability.AGROAL)) {
            additionalBeans
                    .produce(new AdditionalBeanBuildItem.Builder().addBeanClass(SinglestoreAgroalConnectionConfigurer.class)
                            .setDefaultScope(BuiltinScope.APPLICATION.getName())
                            .setUnremovable()
                            .build());
        }
    }

    @BuildStep
    void registerAuthenticationPlugins(BuildProducer<ServiceProviderBuildItem> serviceProvider) {
        // make sure that all plugins are available
        serviceProvider
                .produce(
                        ServiceProviderBuildItem.allProvidersFromClassPath("com.singlestore.jdbc.plugin.AuthenticationPlugin"));
    }

    @BuildStep
    void registerCodecs(BuildProducer<ServiceProviderBuildItem> serviceProvider) {
        serviceProvider
                .produce(ServiceProviderBuildItem.allProvidersFromClassPath("com.singlestore.jdbc.plugin.Codec"));
    }

    @BuildStep(onlyIf = NativeOrNativeSourcesBuild.class)
    void addNativeImageResources(BuildProducer<NativeImageResourceBuildItem> resources) {
        // singlestore.properties is used by com.singlestore.jdbc.util.VersionFactory and is small enough.
        // driver.properties is not added because it only provides optional descriptions for
        // com.singlestore.jdbc.Driver.getPropertyInfo(), which is probably not even called.
        resources.produce(new NativeImageResourceBuildItem("singlestore-jdbc-client.properties"));

        // necessary when jdbcUrl contains useSsl=true
        resources.produce(new NativeImageResourceBuildItem("deprecated.properties"));
    }

    @BuildStep
    void registerServiceBinding(Capabilities capabilities,
            BuildProducer<ServiceProviderBuildItem> serviceProvider) {
        if (capabilities.isPresent(Capability.KUBERNETES_SERVICE_BINDING)) {
            serviceProvider.produce(
                    new ServiceProviderBuildItem("io.quarkus.kubernetes.service.binding.runtime.ServiceBindingConverter",
                            SinglestoreServiceBindingConverter.class.getName()));
        }
    }
}
