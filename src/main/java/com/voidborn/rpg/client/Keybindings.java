package com.voidborn.rpg.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class Keybindings {
    public static final String CATEGORY = "key.categories.voidbornrpg";

    public static final KeyMapping ULTIMATE_KEY = new KeyMapping(
            "key.voidbornrpg.ultimate_skill",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );

    public static final KeyMapping SPECIAL_SKILL_KEY = new KeyMapping(
            "key.voidbornrpg.special_skill",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );
}
