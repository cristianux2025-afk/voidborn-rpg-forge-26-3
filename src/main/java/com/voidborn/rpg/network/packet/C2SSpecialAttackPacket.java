package com.voidborn.rpg.network.packet;

import com.voidborn.rpg.common.capability.PlayerCombatData;
import com.voidborn.rpg.common.item.ShadowDaggersItem;
import com.voidborn.rpg.common.item.VoidGreatswordItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class C2SSpecialAttackPacket {
    private final boolean isSneakingInput;

    public C2SSpecialAttackPacket(boolean isSneakingInput) {
        this.isSneakingInput = isSneakingInput;
    }

    public static void encode(C2SSpecialAttackPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.isSneakingInput);
    }

    public static C2SSpecialAttackPacket decode(FriendlyByteBuf buf) {
        return new C2SSpecialAttackPacket(buf.readBoolean());
    }

    public static void handle(C2SSpecialAttackPacket msg, CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player == null || !player.isAlive() || player.isSpectator()) {
            return;
        }

        PlayerCombatData combatData = PlayerCombatData.get(player);
        if (combatData.isCastingUltimate()) {
            return;
        }

        ItemStack heldStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean serverSneaking = player.isShiftKeyDown() || msg.isSneakingInput;

        if (heldStack.getItem() instanceof VoidGreatswordItem greatsword) {
            if (serverSneaking) {
                greatsword.executeSpecialAttackServer(player, heldStack, combatData);
            } else {
                greatsword.executeComboStepServer(player, heldStack, combatData);
            }
        } else if (heldStack.getItem() instanceof ShadowDaggersItem daggers) {
            if (serverSneaking) {
                daggers.executeShadowStepSpecialServer(player, heldStack, combatData);
            } else {
                daggers.executeRapidComboServer(player, heldStack, combatData);
            }
        }
    }
}
