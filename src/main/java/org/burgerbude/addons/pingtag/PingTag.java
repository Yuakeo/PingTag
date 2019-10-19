package org.burgerbude.addons.pingtag;

import net.labymod.addon.AddonLoader;
import net.labymod.api.LabyModAddon;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.*;
import net.labymod.support.util.Debug;
import net.labymod.utils.Material;
import net.minecraft.entity.player.EntityPlayer;
import org.burgerbude.addons.pingtag.elements.CustomSliderElement;
import org.burgerbude.addons.pingtag.render.PingTagRenderer;
import org.burgerbude.addons.pingtag.render.RainbowStyle;

import java.util.List;
import java.util.UUID;

/**
 * Project: PingTag
 * Author: Robby on 08.10.2019
 */
public class PingTag extends LabyModAddon {

    private PingOMeter pingOMeter;
    private PingTagRenderer tagRenderer;

    private double pingTagSize;
    private boolean rainbow;
    private int rainbowSpeed;
    private String rainbowStyle;

    private UUID damageIndicatorUniqueId;
    private int damageIndicatorScale;
    private int damageIndicatorViewDistance;

    private UUID friendTagsUniqueId;

    @Override
    public void onEnable() {
        try {
            Class<?> damageIndicatorClass = Class.forName("net.labymod.addons.damageindicator.DamageIndicator");
            this.damageIndicatorUniqueId = AddonLoader.getUUIDByClass(damageIndicatorClass);
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class<?> friendTagsClass = Class.forName("de.cerus.friendtags.FriendTags");
            this.friendTagsUniqueId = AddonLoader.getUUIDByClass(friendTagsClass);
        } catch (ClassNotFoundException ignored) {

        }

        this.pingOMeter = new PingOMeter();
        this.tagRenderer = new PingTagRenderer(this);

        this.getApi().registerForgeListener(this.pingOMeter);

        this.getApi().getEventManager().register((entity, positionX, positionY, positionZ, partialTicks) -> {
            if (!(entity instanceof EntityPlayer)) return;

            this.tagRenderer.renderTag((EntityPlayer) entity, positionX, positionY, positionZ, partialTicks);
        });
    }

    @Override
    public void loadConfig() {
        this.pingTagSize = this.getConfig().has("pingTagSize") ? this.getConfig().get("pingTagSize").getAsDouble() : 1.6D;
        this.rainbow = this.getConfig().has("rainbow") && this.getConfig().get("rainbow").getAsBoolean();
        this.rainbowSpeed = this.getConfig().has("rainbowSpeed") ? this.getConfig().get("rainbowSpeed").getAsInt() : 12;
        this.rainbowStyle = this.getConfig().has("rainbowStyle") ? this.getConfig().get("rainbowStyle").getAsString() : RainbowStyle.NORMAL.name();
    }

    @Override
    protected void fillSettings(List<SettingsElement> subSettings) {
        CustomSliderElement pingTagSizeElement = new CustomSliderElement("Tag Size", this, new ControlElement.IconData(Material.GLASS), "pingTagSize", this.pingTagSize);
        pingTagSizeElement.setDescriptionText("0.8 ... 1.6");
        pingTagSizeElement.range(0.8, 1.6);
        pingTagSizeElement.addCallback(accepted -> {
            pingTagSize = accepted;
            getConfig().addProperty("pingTagSize", pingTagSize);
            saveConfig();
        });

        subSettings.add(pingTagSizeElement);

        BooleanElement rainbowElement = new BooleanElement("Enable Rainbow", this, new ControlElement.IconData(Material.LEVER), "rainbow", this.rainbow);
        rainbowElement.addCallback(accepted -> {
            rainbow = accepted;
            getConfig().addProperty("rainbow", rainbow);
            saveConfig();
        });

        subSettings.add(rainbowElement);

        SliderElement rainbowSpeedElement = new SliderElement("Rainbow Speed", this, new ControlElement.IconData(Material.SUGAR), "rainbowSpeed", this.rainbowSpeed);
        rainbowSpeedElement.setRange(1, 25);
        rainbowSpeedElement.addCallback(accepted -> {
            rainbowSpeed = accepted;
            getConfig().addProperty("rainbowSpeed", rainbowSpeed);
            saveConfig();
        });

        subSettings.add(rainbowSpeedElement);

        final DropDownMenu<RainbowStyle> rainbowDropDownMenu = new DropDownMenu<RainbowStyle>("Rainbow Style" /* Display name */, 0, 0, 0, 0)
                .fill(RainbowStyle.values());
        DropDownElement<RainbowStyle> rainbowDropDown = new DropDownElement<>("Rainbow Style", rainbowDropDownMenu);

        rainbowDropDownMenu.setSelected(RainbowStyle.NORMAL);

        rainbowDropDown.setChangeListener(style -> {
            this.rainbowStyle = style.name();
            getConfig().addProperty("rainbowStyle", this.rainbowStyle);
            saveConfig();
        });

        rainbowDropDownMenu.setEntryDrawer((object, x, y, trimmedEntry) -> {
            String entry = object.toString().substring(0, 1) + object.toString().substring(1).toLowerCase();
            LabyMod.getInstance().getDrawUtils().drawString(entry, x, y);
        });

        subSettings.add(rainbowDropDown);
    }

    public PingOMeter pingOMeter() {
        return this.pingOMeter;
    }

    public double pingTagSize() {
        return this.pingTagSize;
    }

    public boolean rainbow() {
        return this.rainbow;
    }

    public int rainbowSpeed() {
        return this.rainbowSpeed;
    }

    public RainbowStyle rainbowStyle() {
        return RainbowStyle.name(this.rainbowStyle);
    }

    public boolean damageIndicatorIsActive() {
        LabyModAddon addon = AddonLoader.getAddonByUUID(damageIndicatorUniqueId);

        damageIndicatorScale = addon.getConfig().has("scale") ? addon.getConfig().get("scale").getAsInt() : 100;
        damageIndicatorViewDistance = addon.getConfig().has("distance") ? addon.getConfig().get("distance").getAsInt() : 50;

        return addon.getConfig().has("visible") && addon.getConfig().get("visible").getAsBoolean();
    }

    public boolean friendTagsIsActive() {
        LabyModAddon addon = AddonLoader.getAddonByUUID(friendTagsUniqueId);
        return !addon.getConfig().has("enabled") || addon.getConfig().get("enabled").getAsBoolean();
    }

    public int damageIndicatorViewDistance() {
        return damageIndicatorViewDistance;
    }

    public int damageIndicatorScale() {
        return damageIndicatorScale;
    }
}
