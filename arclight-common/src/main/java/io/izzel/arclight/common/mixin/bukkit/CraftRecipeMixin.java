package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.core.world.item.crafting.IngredientBridge;
import io.izzel.arclight.common.mod.inventory.ArclightSpecialIngredient;
import net.minecraft.world.item.crafting.Ingredient;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v.util.CraftMagicNumbers;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = CraftRecipe.class, remap = false)
public interface CraftRecipeMixin {

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    default Ingredient toNMS(RecipeChoice bukkit, boolean requireNotEmpty) {
        Ingredient stack;
        switch (bukkit) {
            case null -> stack = Ingredient.EMPTY;
            case RecipeChoice.MaterialChoice materialChoice ->
                    stack = new Ingredient(materialChoice.getChoices().stream().map((mat) -> new Ingredient.ItemValue(CraftItemStack.asNMSCopy(new ItemStack(mat)))));
            case RecipeChoice.ExactChoice exactChoice -> {
                stack = new Ingredient(exactChoice.getChoices().stream().map((mat) -> new Ingredient.ItemValue(CraftItemStack.asNMSCopy(mat))));
                ((IngredientBridge) (Object) stack).bridge$setExact(true);
            }
            case ArclightSpecialIngredient arclightSpecialIngredient ->
                    stack = arclightSpecialIngredient.getIngredient();
            default -> throw new IllegalArgumentException("Unknown recipe stack instance " + bukkit);
        }

        stack.getItems();
        if (stack.getClass() == Ingredient.class && requireNotEmpty && stack.getItems().length == 0) {
            throw new IllegalArgumentException("Recipe requires at least one non-air choice!");
        } else {
            return stack;
        }
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    static RecipeChoice toBukkit(Ingredient list) {
        list.getItems();
        if (list.getClass() != Ingredient.class) {
            return new ArclightSpecialIngredient(list);
        }
        net.minecraft.world.item.ItemStack[] items = list.getItems();
        if (items.length == 0) {
            return null;
        } else {
            if (((IngredientBridge) (Object) list).bridge$isExact()) {
                List<ItemStack> choices = new ArrayList<>(items.length);
                for (net.minecraft.world.item.ItemStack i : items) {
                    choices.add(CraftItemStack.asBukkitCopy(i));
                }
                return new RecipeChoice.ExactChoice(choices);
            } else {
                List<org.bukkit.Material> choices = new ArrayList<>(items.length);
                for (net.minecraft.world.item.ItemStack i : items) {
                    choices.add(CraftMagicNumbers.getMaterial(i.getItem()));
                }
                return new RecipeChoice.MaterialChoice(choices);
            }
        }
    }
}
