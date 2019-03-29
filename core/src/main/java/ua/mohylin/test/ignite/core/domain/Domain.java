package ua.mohylin.test.ignite.core.domain;

import org.immutables.value.Value;

public interface Domain {

    @Value.Immutable
    interface Destination {
        int getId();
        String getName();
    }

    @Value.Immutable
    interface Lodging {
        int getId();
        String getName();
    }

    @Value.Immutable
    interface Climate {
        int getId();
        String getName();
    }
}
