package ua.mohylin.test.ignite.prerequisite;



import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * There's an issue with Ignite-osgi that it's not going to be resolved after Karaf restarts.
 * This activator checks it's status and if not RESOLVED, refresh is triggered on ignite-core.
 *
 * @author olemogyl
 * Created on Mar 21, 2019
 */
@Component
public class PrerequisiteActivator implements BundleActivator {

    private static final Logger log = LoggerFactory.getLogger(PrerequisiteActivator.class);

    @Override
    public void start(BundleContext bcx) throws Exception {

        logMessage("Ignite-osgi workaround activator launches");

        final Bundle systemBundle = bcx.getBundle(0);
        final List<Bundle> installedBundles = Arrays.asList(bcx.getBundles());

        final List<Bundle> igniteBundles = installedBundles.stream()
            .filter(b -> b.getSymbolicName().startsWith("org.apache.ignite"))
            .collect(Collectors.toList());

        final Optional<Bundle> igniteOsgiBundle = igniteBundles.stream()
            .filter(b ->  b.getSymbolicName().endsWith("ignite-osgi") )
            .findFirst();

        final Optional<Bundle> igniteCoreBundle = igniteBundles.stream()
            .filter(b ->  b.getSymbolicName().endsWith("ignite-core") )
            .findFirst();


        if ( !igniteOsgiBundle.isPresent()) {
            logMessage("Bundle ignite-osgi is not loaded, workaround check is skipped");
        } else if ( !igniteCoreBundle.isPresent()) {
            logMessage("Bundle ignite-core is not loaded, workaround check is skipped");
        } else  if ( igniteOsgiBundle.get().getState() != Bundle.RESOLVED) {

            logMessage("Forcing bundle:refresh on ignite-core to resolve ignite-osgi...");

            FrameworkWiring frameworkWiring = systemBundle.adapt(FrameworkWiring.class);
            frameworkWiring.refreshBundles(Arrays.asList(igniteCoreBundle.get()));

        }
    }

    private void logMessage(String x) {
        System.out.println(x);
        log.info(x);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        //does nothing
    }
}
