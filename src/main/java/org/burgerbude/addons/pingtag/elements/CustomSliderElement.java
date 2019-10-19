package org.burgerbude.addons.pingtag.elements;

import net.labymod.api.LabyModAddon;
import net.labymod.main.LabyMod;
import net.labymod.main.ModSettings;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.function.Consumer;

/**
 * Project: PingTag
 * Author: Robby on 19.10.2019
 */
public class CustomSliderElement extends ControlElement {

    private Double currentValue;

    private Consumer<Double> changeListener;
    private Consumer<Double> callback;

    private double minValue;
    private double maxValue;

    private boolean dragging;
    private boolean hover;

    private double dragValue;
    private double steps;


    public CustomSliderElement(String elementName, String configEntryName, IconData iconData) {
        super(elementName, configEntryName, iconData);
        this.minValue = 0;
        this.maxValue = 2;
        this.steps = 0.1;
        if (!configEntryName.isEmpty()) {
            try {
                this.currentValue = (Double) ModSettings.class.getDeclaredField(configEntryName).get(LabyMod.getSettings());
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        if (this.currentValue == null) this.currentValue = this.minValue;

        this.changeListener = accepted -> {
            try {
                ModSettings.class.getDeclaredField(configEntryName).set(LabyMod.getSettings(), accepted);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        };
    }

    public CustomSliderElement(String displayName, IconData iconData, double currentValue) {
        super(displayName, null, iconData);
        this.minValue = 0;
        this.maxValue = 2;
        this.steps = 0.1;
        this.currentValue = currentValue;
        this.changeListener = accepted -> {
            if (callback != null) callback.accept(accepted);
        };
    }

    public CustomSliderElement(String displayName, LabyModAddon addon, IconData iconData, String attribute, double currentValue) {
        super(displayName, iconData);
        this.minValue = 0;
        this.maxValue = 2;
        this.steps = 0.1;

        this.currentValue = currentValue;

        this.changeListener = accepted -> {
            addon.getConfig().addProperty(attribute, accepted);
            addon.loadConfig();

            if (callback != null) callback.accept(accepted);
        };
    }

    public CustomSliderElement minValue(double minValue) {
        this.minValue = minValue;

        if (this.currentValue < this.minValue) this.currentValue = this.minValue;
        return this;
    }

    public CustomSliderElement maxValue(double maxValue) {
        this.maxValue = maxValue;

        if (this.currentValue > this.maxValue) this.currentValue = this.maxValue;
        return this;
    }

    public CustomSliderElement range(double min, double max) {
        this.minValue(min);
        this.maxValue(max);
        return this;
    }

    public CustomSliderElement steps(double steps) {
        this.steps = steps;
        return this;
    }

    public CustomSliderElement addCallback(Consumer<Double> callback) {
        this.callback = callback;
        return this;
    }

    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }

    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        super.draw(x, y, maxX, maxY, mouseX, mouseY);

        DrawUtils draw = LabyMod.getInstance().getDrawUtils();

        int width = this.getObjectWidth();

        if (this.displayName != null) {
            draw.drawRectangle(x - 1, y, x, maxY, ModColor.toRGB(120, 120, 120, 120));
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(buttonTextures);
        GlStateManager.color(1.0F, 1.0F, 1.0F);

        double maxSliderPos = (double) maxX;
        double sliderWidth = (double) (width - 8);
        double sliderWidthBackground = (double) width;
        double minSliderPos = maxSliderPos - (double) width;
        double totalValueDiff = this.maxValue - this.minValue;
        double currentValue = this.currentValue;
        double pos = minSliderPos + sliderWidth / totalValueDiff * (currentValue - this.minValue);

        draw.drawTexturedModalRect(minSliderPos, (double) (y + 1), 0.0D, 46.0D, sliderWidthBackground / 2.0D, 20.0D);
        draw.drawTexturedModalRect(minSliderPos + sliderWidthBackground / 2.0D, (double) (y + 1), 200.0D - sliderWidthBackground / 2.0D, 46.0D, sliderWidthBackground / 2.0D, 20.0D);

        this.hover = mouseX > x && mouseX < maxX && mouseY > y + 1 && mouseY < maxY;

        draw.drawTexturedModalRect(pos, (double) (y + 1), 0.0D, 66.0D, 4.0D, 20.0D);
        draw.drawTexturedModalRect(pos + 4.0D, (double) (y + 1), 196.0D, 66.0D, 4.0D, 20.0D);

        if (!this.isMouseOver()) {
            this.mouseRelease(mouseX, mouseY, 0);
        } else {
            double mouseToMinSlider = (double) mouseX - minSliderPos;
            double finalValue = this.minValue + totalValueDiff / sliderWidth * (mouseToMinSlider - 1.0D);
            if (this.dragging) {
                this.dragValue = finalValue;
                this.mouseClickMove(mouseX, mouseY, 0);
            }
        }

        draw.drawCenteredString("" + this.currentValue, minSliderPos + sliderWidthBackground / 2.0D, (double) (y + 7));
    }

    public void unfocus(int mouseX, int mouseY, int mouseButton) {
        super.unfocus(mouseX, mouseY, mouseButton);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.hover) {
            this.dragging = true;
        }

    }

    public void mouseRelease(int mouseX, int mouseY, int mouseButton) {
        super.mouseRelease(mouseX, mouseY, mouseButton);
        if (this.dragging) {
            this.dragging = false;

            this.currentValue = Math.round((this.dragValue / this.steps) * this.steps * 100.0) / 100.0;
            if (this.currentValue > this.maxValue) {
                this.currentValue = this.maxValue;
            }

            if (this.currentValue < this.minValue) {
                this.currentValue = this.minValue;
            }

            this.changeListener.accept(this.currentValue);
        }

    }

    public void mouseClickMove(int mouseX, int mouseY, int mouseButton) {
        super.mouseClickMove(mouseX, mouseY, mouseButton);
        if (this.dragging) {
            this.currentValue = Math.round(((this.dragValue / this.steps) * this.steps) * 100.0) / 100.0;
            if (this.currentValue > this.maxValue) {
                this.currentValue = this.maxValue;
            }

            if (this.currentValue < this.minValue) {
                this.currentValue = this.minValue;
            }

            this.changeListener.accept(this.currentValue);
        }

    }

    @Override
    public int getObjectWidth() {
        return 50;
    }
}
