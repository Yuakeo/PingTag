package org.burgerbude.addons.pingtag.render;

import net.labymod.labyconnect.user.ChatUser;
import net.labymod.main.LabyMod;
import net.labymod.user.User;
import net.labymod.user.group.EnumGroupDisplayType;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.burgerbude.addons.pingtag.PingTag;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Objects;
import java.util.UUID;

/**
 * Project: PingTag
 * Author: Robby on 08.10.2019
 */
public class PingTagRenderer {

    private PingTag addon;
    private RenderManager renderManager;

    public PingTagRenderer(PingTag addon) {
        this.addon = addon;
        this.renderManager = Minecraft.getMinecraft().getRenderManager();
    }

    public void renderTag(EntityPlayer player, double posX, double posY, double posZ, float partialTicks) {
        double distance = player.getDistanceSq(this.renderManager.renderViewEntity);

        int ping = this.addon.pingOMeter().playerPing(player.getUniqueID());

        if (player == Minecraft.getMinecraft().player && Minecraft.getMinecraft().gameSettings.hideGUI) return;

        if (!player.isSneaking() && distance <= 64 * 64 && ping > 0) {

            // fix the mc bug
            float fixedPlayerViewX = this.renderManager.playerViewX * (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? -1 : 1);

            FontRenderer fontRenderer = this.addon.getApi().getDrawUtils().getFontRenderer();

            GlStateManager.pushMatrix();

            User user = this.addon.getApi().getUserManager().getUser(player.getUniqueID());

            double height = player.height + .8;

            if (player.isSneaking()) height += .03F;

            //LabyMod Stuff
            if (user != null && LabyMod.getSettings().cosmetics) height += user.getMaxNameTagHeight();
            if (Objects.requireNonNull(user).getGroup() != null && user.getGroup().getDisplayType() == EnumGroupDisplayType.ABOVE_HEAD)
                height += 0.129F;
            if (user.getSubTitle() != null) height += user.getSubTitleSize() / 6 - .025F;

            //DamageIndicator

            if (64 * 64 >= addon.damageIndicatorViewDistance() * addon.damageIndicatorViewDistance()) {
                if (this.addon.damageIndicatorIsActive() && player != Minecraft.getMinecraft().player) {
                    height += ((double) this.addon.damageIndicatorScale() / 100) * .23;
                }
            }

            //Friend Tags
            if (this.addon.friendTagsIsActive() && player != Minecraft.getMinecraft().player && isFriend(player.getUniqueID())) {
                height += .23;
            }

            //Scoreboard Hearts
            if (distance < 10) {
                Scoreboard scoreboard = player.getWorldScoreboard();
                ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(2);

                if (scoreObjective != null) {
                    height += fontRenderer.FONT_HEIGHT * 1.15F * 0.026666667F;
                }
            }

            height += this.addon.pingTagSize() / 6 - .25;

            //Move to the player position
            GlStateManager.translate(posX, posY + height, posZ);

            GL11.glNormal3f(0.0F, 1.0F, 0.0F);

            GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(fixedPlayerViewX, 1.0F, 0.0F, 0.0F);

            float scale = (float) (0.016F * this.addon.pingTagSize());
            GlStateManager.scale(-scale, -scale, scale);

            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);

            String text = (this.addon.rainbow() ? "" : pingColor(ping)) + "" + ping + " ms";
            int textPosition = fontRenderer.getStringWidth(text) / 2;

            this.addon.getApi().getDrawUtils().drawRect(-textPosition - 1, -1, textPosition + 1, 9, new Color(0.0F, 0.0F, 0.0F, .25F).hashCode());

            GlStateManager.enableBlend();

            GlStateManager.depthMask(true);

            if (this.addon.rainbow()) {
                switch (addon.rainbowStyle()) {
                    case NORMAL:
                        fontRenderer.drawString(text, -fontRenderer.getStringWidth(text) / 2, 0, rainbow());
                        break;
                    case WAVE:
                        waveString(fontRenderer, text, -fontRenderer.getStringWidth(text) / 2, 0);
                        break;
                }
            } else {
                fontRenderer.drawString(text, -fontRenderer.getStringWidth(text) / 2, 0, -1);
            }


            GlStateManager.disableBlend();

            GlStateManager.enableLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            GlStateManager.popMatrix();
        }
    }

    private ModColor pingColor(int ping) {

        if (ping <= 50) {
            return ModColor.GREEN;
        } else if (ping > 51 && ping <= 75) {
            return ModColor.YELLOW;
        } else if (ping > 76 && ping <= 125) {
            return ModColor.RED;
        } else if (ping > 126) {
            return ModColor.DARK_RED;
        }

        return ModColor.GREEN;
    }

    private int rainbow() {
        return rainbow(0);
    }

    private int rainbow(int offset) {
        int speed = this.addon.rainbowSpeed() * 1000;
        float hue = (float) ((System.currentTimeMillis() + offset) % speed) / speed;
        return Color.HSBtoRGB(hue, 0.8F, 0.8F);
    }

    private void waveString(FontRenderer fontRenderer, String text, int x, int y) {
        for (char c : text.toCharArray()) {
            fontRenderer.drawString(String.valueOf(c), x, y, rainbow(x * -fontRenderer.getStringWidth(text)));
            x += fontRenderer.getStringWidth(String.valueOf(c));
        }
    }

    private boolean isFriend(UUID uniqueId) {
        for (ChatUser chatUser : LabyMod.getInstance().getLabyConnect().getFriends()) {
            if (chatUser.getGameProfile().getId().toString().equals(uniqueId.toString())) {
                return true;
            }
        }
        return false;
    }
}
