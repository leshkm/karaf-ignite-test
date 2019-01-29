## Changes to karaf configuration

    add jdk.internal.misc to boot delegation (etc/config.properties)

add

    sun.nio.ch, \
    sun.misc, \
    com.sun.management

to exposed jre platform exports


add

    --add-opens java.base/jdk.internal.misc=ALL-UNNAMED \

to bin/karaf to expose jdk.internal.misc to the unnamed module

## Installation into running karaf

Go to your local repositiory to review the available versions.

    cd ~/.m2/repository/se/reseguiden/graphql/graphql-features/

Inside each version dir there is located an xml describing the feature.

    vim ~/.m2/repository/se/reseguiden/graphql/graphql-features/0.0.1/graphql-features-0.0.1-features.xml

Open it and review the dependancy relations. You will find that graphql-core pulls in the other dependancies. Start karaf and add the feature and install it.

    karaf@root()> feature:repo-add mvn:se.reseguiden.graphql/graphql-features/0.0.1/xml/features
    karaf@root()> feature:install graphql-core
