package io.izzel.arclight.common.bridge.core.network;

import com.mojang.authlib.properties.Property;

import java.util.UUID;

public interface NetworkManagerBridge {

    UUID bridge$getSpoofedUUID();

    void bridge$setSpoofedUUID(UUID spoofedUUID);

    Property[] bridge$getSpoofedProfile();

    void bridge$setSpoofedProfile(Property[] spoofedProfile);

    String bridge$getHostname();

    void bridge$setHostname(String hostname);
}
