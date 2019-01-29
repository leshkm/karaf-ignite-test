package ua.mohylin.test.ignite.core.cache;
import java.util.Collection;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.osgi.IgniteOsgiUtils;
import org.apache.ignite.osgi.classloaders.BundleDelegatingClassLoader;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Oleksii: This class somewhat repeats IgniteAbstractOsgiContextActivator, but I'll try to
 * have it as BundleActivator as well.
 *
 * @author issal
 * Created Aug 9, 2018
 */
@Component
public class IgniteLifecycleComponent implements BundleActivator {

    private static final Logger log = LoggerFactory.getLogger(IgniteLifecycleComponent.class);
    private Ignite ignite;


    //@Activate
    public void activate(BundleContext bundleContext) {
        log.info("activating ignite");

        // shutdown any existing local ignite instances
        // this only happens if (de)activation failed
        if (IgniteOsgiUtils.gridCount() > 0) {
            Ignition.stop(true); // kills any running jobs
        }

        BundleDelegatingClassLoader compositeClassLoader = new BundleDelegatingClassLoader(
            bundleContext.getBundle(), Ignite.class.getClassLoader()
        );

        // this is the brute force method of classloading
        // basically every single in bundle is in ignites classloader
//        ContainerSweepClassLoader compositeClassLoader = new ContainerSweepClassLoader(
//            bundleContext.getBundle(), Ignite.class.getClassLoader()
//        );

        var destinationCacheConfig = new CacheConfiguration()
            .setName("destination");
//            .setCacheLoaderFactory(destinationLoader) // requires sweep class loader
//            .setSqlEscapeAll(true)
//           .setIndexedTypes(String.class, ImmutableDestination.class);

        var lodgingCacheConfig = new CacheConfiguration()
            .setName("lodging");
//            .setIndexedTypes(String.class, ImmutableLodging.class);

        var climateCacheConfig = new CacheConfiguration()
            .setName("climate");

        IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
        igniteConfiguration.setClassLoader(compositeClassLoader);
        igniteConfiguration.setCacheConfiguration(
            destinationCacheConfig,
            lodgingCacheConfig,
            climateCacheConfig
        );

        ignite = Ignition.start(igniteConfiguration);

        IgniteCache destinationCache = ignite.getOrCreateCache(destinationCacheConfig);

//        destinationCache.clear();

        try {
            Collection<QueryEntity> entities = destinationCacheConfig.getQueryEntities();
            log.info("{}", entities);
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        log.info("ignite activation done");
    }

    //@Deactivate
    public void deactivate() {
        log.info("deactivating ignite");
        ignite.close();
        ignite = null;
        log.info("ignite deactivation done");
    }

    @Modified
    public void modify() {
        // empty
    }

    public IgniteConfiguration igniteConfiguration() {
        return null;
    }

    @Override
    public void start(BundleContext context) {
        activate(context);
    }

    @Override
    public void stop(BundleContext context) {
        deactivate();
    }
}
