package com.voidborn.rpg.client;

import com.voidborn.rpg.common.capability.PlayerCombatData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public class CombatHudOverlay {
    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui || player.isSpectator()) {
            return;
        }

        PlayerCombatData data = PlayerCombatData.get(player);
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int barWidth = 110;
        int barHeight = 8;
        int x = 14;
        int y = screenHeight - 38;

        guiGraphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xCC120824);

        float manaRatio = Math.max(0.0F, Math.min(1.0F, data.getCurrentMana() / Math.max(1.0F, data.getMaxMana())));
        int filledWidth = (int) (barWidth * manaRatio);
        guiGraphics.fillGradient(x, y, x + filledWidth, y + barHeight, 0xFFB84DFF, 0xFF5A189A);

        String manaText = String.format("Maná: %d / %d", (int) data.getCurrentMana(), (int) data.getMaxMana());
        guiGraphics.drawString(mc.font, manaText, x, y - 11, 0xFFE0AAFF, true);

        if (data.getComboStage() > 0) {
            guiGraphics.drawString(mc.font, "COMBO x" + data.getComboStage(), x + barWidth + 10, y - 1, 0xFFFFD166, true);
        }

        String ultStatus = data.getUltimateCooldown() == 0
                ? "[R] Ultimate: LISTA"
                : String.format("[R] Ultimate: %.1fs", data.getUltimateCooldown() / 20.0F);
        int ultColor = data.getUltimateCooldown() == 0 ? 0xFF80FFDB : 0xFFADB5BD;
        guiGraphics.drawString(mc.font, ultStatus, x, y + 11, ultColor, true);
    }
}
