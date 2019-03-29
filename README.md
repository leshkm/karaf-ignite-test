This project made to help with debugging problem with Ignite 2.7.0 in Karaf 4.2.0.

---

ISSUE 2 - 

Two of these processes cannot create cluster. 
Problem lays in org.apache.ignite.internal.processors.cluster.GridClusterStateProcessor.validateNode

When it tries to unmarshal joining node data, it uses `Thread.currentThread().getContextClassLoader()`
 to obtain classloader. This returns system classloader `jdk.internal.loader.ClassLoaders$AppClassLoader` and not bundle classloader created in  `IgniteAbstractOsgiContextActivator.start()`.

Of course, system classloader doesn't know anything about classes from ignite-osgi bundle....
This is the exception:

    Error on unmarshalling discovery data from node 10.0.2.15,127.0.0.1,172.17.0.1:47501: Failed to find class with given class loader for unmarshalling (make sure same versions of all classes are available on all nodes or enable peer-class-loading) [clsLdr=jdk.internal.loader.ClassLoaders$AppClassLoader@5c0369c4, cls=org.apache.ignite.internal.processors.cluster.DiscoveryDataClusterState]; node is not allowed to join

When in debug from IDEA I use proper classloader stored in `IgniteOsgiUtils.CLASSLOADERS`, unmarshalling works without any problem.  

--- 
Solved ISSUE - Solved by adding ignite-osgi and changing build process not to include classloaders folder into import. 

During initial start this configuration performs fine (see karaf_log_install.log for full log). 

    karaf@root()> feature:list | grep "Started"
    ...
    ki-test-core                             │ 0.0.1            │ x        │ Started     │ ki-test-features                  │ graphql core sandbox
    ignite-core                              │ 2.7.0            │          │ Started     │ ignite                            │ Apache Ignite :: Core
    ...
    
    karaf@root()> bundle:list | grep "ignite"
     85 │ Active   │  80 │ 2.7.0              │ ignite-core, Fragments: 86
     86 │ Resolved │  80 │ 2.7.0              │ ignite-osgi, Hosts: 85

But when I do restart of Karaf, it won't start.

There's exception in log (see karaf_log_restart.log for full log)

    2019-01-29T16:14:27,448 | ERROR | FelixDispatchQueue | ki-test-core                     | 70 - ki-test-core - 0.0.1 | FrameworkEvent ERROR - ki-test-core
    org.osgi.framework.BundleException: Unable to resolve ki-test-core [70](R 70.0): missing requirement [ki-test-core [70](R 70.0)] osgi.wiring.package; (&(osgi.wiring.package=org.apache.ignite.osgi.classloaders)(version>=2.7.0)(!(version>=3.0.0))) [caused by: Unable to resolve org.apache.ignite.ignite-osgi [86](R 86.0): missing requirement [org.apache.ignite.ignite-osgi [86](R 86.0)] osgi.wiring.host; (&(osgi.wiring.host=org.apache.ignite.ignite-core)(bundle-version>=0.0.0))] Unresolved requirements: [[ki-test-core [70](R 70.0)] osgi.wiring.package; (&(osgi.wiring.package=org.apache.ignite.osgi.classloaders)(version>=2.7.0)(!(version>=3.0.0)))]
        at org.apache.felix.framework.Felix.resolveBundleRevision(Felix.java:4149) ~[?:?]
        at org.apache.felix.framework.Felix.startBundle(Felix.java:2119) ~[?:?]
        at org.apache.felix.framework.Felix.setActiveStartLevel(Felix.java:1373) ~[?:?]
        at org.apache.felix.framework.FrameworkStartLevelImpl.run(FrameworkStartLevelImpl.java:308) ~[?:?]
        at java.lang.Thread.run(Thread.java:844) [?:?]
        
And, of course, bundle is not resolved and Ignite is not started

    karaf@root()> bundle:list | grep "ignite"
     85 │ Active    │  80 │ 2.7.0              │ ignite-core
     86 │ Installed │  80 │ 2.7.0              │ ignite-osgi