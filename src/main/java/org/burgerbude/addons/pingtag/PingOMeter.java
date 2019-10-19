package org.burgerbude.addons.pingtag;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

/**
 * Project: PingTag
 * Author: Robby on 13.10.2019
 */
public class PingOMeter {

    private Map<UUID, Integer> pingMap;

    private Minecraft minecraft;
    private long lastPingUpdate;

    public PingOMeter() {
        this.pingMap = new HashMap<>();
        this.minecraft = Minecraft.getMinecraft();
    }

    @SubscribeEvent
    public void updatePing(TickEvent.ClientTickEvent event) {
        if (minecraft.player == null) return;

        if (event.phase == TickEvent.Phase.END) {
            if (this.lastPingUpdate + 5000L <= System.currentTimeMillis()) {
                this.lastPingUpdate = System.currentTimeMillis();

                Objects.requireNonNull(minecraft.getConnection()).getPlayerInfoMap().forEach(networkPlayerInfo -> {
                    if (networkPlayerInfo.getGameProfile().getId().getLeastSignificantBits() == 0L) return;

                    pingMap.put(networkPlayerInfo.getGameProfile().getId(), networkPlayerInfo(networkPlayerInfo.getGameProfile().getName()).getResponseTime());
                });
            }
        }
    }

    public Integer playerPing(UUID uniqueId) {
        if (this.pingMap == null || this.pingMap.get(uniqueId) == null) return 0;

        return this.pingMap.get(uniqueId);
    }

    public NetworkPlayerInfo networkPlayerInfo(String name) {
        Collection<NetworkPlayerInfo> collection = Objects.requireNonNull(minecraft.getConnection()).getPlayerInfoMap();
        return collection.stream().filter(networkPlayerInfo -> networkPlayerInfo.getGameProfile().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Map<UUID, Integer> pingMap() {
        return pingMap;
    }
}
