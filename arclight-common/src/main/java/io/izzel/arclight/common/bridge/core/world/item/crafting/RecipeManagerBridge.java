package io.izzel.arclight.common.bridge.core.world.item.crafting;

import net.minecraft.world.item.crafting.RecipeHolder;

public interface RecipeManagerBridge {

    void bridge$addRecipe(RecipeHolder<?> recipe);

    void bridge$clearRecipes();
}
