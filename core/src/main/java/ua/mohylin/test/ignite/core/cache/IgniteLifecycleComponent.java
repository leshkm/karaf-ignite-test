package ua.mohylin.test.ignite.core.cache;

import java.util.Collection;

import com.google.common.collect.ImmutableList;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.osgi.IgniteAbstractOsgiContextActivator;
import org.apache.ignite.osgi.IgniteOsgiUtils;
import org.apache.ignite.osgi.classloaders.BundleDelegatingClassLoader;
import org.apache.ignite.osgi.classloaders.ContainerSweepClassLoader;
import org.apache.ignite.osgi.classloaders.OsgiClassLoadingStrategyType;
import org.apache.ignite.spi.communication.CommunicationSpi;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.DiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.mohylin.test.ignite.core.domain.ImmutableDestination;
import ua.mohylin.test.ignite.core.domain.ImmutableLodging;


/**
 *
 * @author issal
 * Created Aug 9, 2018
 */
@Component(immediate = true)
public class IgniteLifecycleComponent extends IgniteAbstractOsgiContextActivator {

    private static final Logger log = LoggerFactory.getLogger(IgniteLifecycleComponent.class);
    private static Ignite ignite;
    @Activate
    public void activate(BundleContext bundleContext) {
        log.info("activating ignite");
        /*
        // shutdown any existing local ignite instances
        // this only happens if (de)activation failed
        if (IgniteOsgiUtils.gridCount() > 0) {
            Ignition.stop(true); // kills any running jobs
        }

        BundleDelegatingClassLoader compositeClassLoader = new ContainerSweepClassLoader(
            bundleContext.getBundle(), Ignite.class.getClassLoader()
        );

        // this is the brute force method of classloading
        // basically every single in bundle is in ignites classloader
//        ContainerSweepClassLoader compositeClassLoader = new ContainerSweepClassLoader(
//            bundleContext.getBundle(), Ignite.class.getClassLoader()
//        );

        var igniteConfiguration = igniteConfiguration();
        igniteConfiguration.setPeerClassLoadingEnabled(true);
        igniteConfiguration.setClassLoader(compositeClassLoader);

        ignite = Ignition.start(igniteConfiguration);

//        IgniteCache destinationCache = ignite.getOrCreateCache(destinationCacheConfig);

//        destinationCache.clear();

//        try {
//            Collection<QueryEntity> entities = destinationCacheConfig.getQueryEntities();
//            log.info("{}", entities);
//        } catch (Exception ex) {
//            log.error("{}", ex);
//        }
        // Add into Ignite's OSGi registry.

        // Add into Ignite's OSGi registry.
        IgniteOsgiUtils.classloaders().put(ignite, compositeClassLoader);
        */
        log.info("ignite activation done");
    }

    @Deactivate
    public void deactivate() {
        log.info("deactivating ignite");
        //ignite.close();
        ignite = null;
        log.info("ignite deactivation done");
    }

    @Modified
    public void modify() {
        // empty
    }

    @Override
    public IgniteConfiguration igniteConfiguration() {

        var destinationCacheConfig = new CacheConfiguration()
            .setName("destination")
//            .setCacheLoaderFactory(destinationLoader) // requires sweep class loader
//            .setSqlEscapeAll(true)
            .setIndexedTypes(String.class, ImmutableDestination.class);

        var lodgingCacheConfig = new CacheConfiguration()
            .setName("lodging")
            .setIndexedTypes(String.class, ImmutableLodging.class);

        var climateCacheConfig = new CacheConfiguration()
            .setName("climate");

        var igniteConfiguration = new IgniteConfiguration();

        igniteConfiguration.setFailureDetectionTimeout(60000);

        igniteConfiguration.setClientMode(true);
        igniteConfiguration.setDaemon(false);

        CommunicationSpi commSpi = new TcpCommunicationSpi();
        igniteConfiguration.setCommunicationSpi(commSpi);

        var discoSpi = new TcpDiscoverySpi();
        discoSpi.setJoinTimeout(120000);


        igniteConfiguration.setDiscoverySpi(discoSpi);
        //igniteConfiguration.setPeerClassLoadingEnabled(true);

        igniteConfiguration.setCacheConfiguration(
            destinationCacheConfig,
            lodgingCacheConfig,
            climateCacheConfig
        );

        return igniteConfiguration;
    }

    @Override
    public OsgiClassLoadingStrategyType classLoadingStrategy() {
        return OsgiClassLoadingStrategyType.CONTAINER_SWEEP;
    }

    protected void onAfterStart(BundleContext ctx, @Nullable Throwable t) {
        var sRef = ctx.getServiceReference(Ignite.class);
        ignite = ctx.getService(sRef);
    }
}
