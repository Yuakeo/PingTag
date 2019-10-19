package org.burgerbude.addons.pingtag.render;

import java.util.Arrays;

/**
 * Project: PingTag
 * Author: Robby on 19.10.2019
 */
public enum RainbowStyle {

    NORMAL,
    WAVE,;

    public static RainbowStyle name(String name) {
        return Arrays.stream(values()).filter(rainbowStyle -> rainbowStyle.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

}
