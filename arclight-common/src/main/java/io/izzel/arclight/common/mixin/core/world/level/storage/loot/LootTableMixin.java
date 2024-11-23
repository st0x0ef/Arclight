package io.izzel.arclight.common.mixin.core.world.level.storage.loot;

import io.izzel.arclight.common.bridge.core.world.storage.loot.LootTableBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.bukkit.craftbukkit.v.CraftLootTable;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.event.world.LootGenerateEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Mixin(LootTable.class)
public abstract class LootTableMixin implements LootTableBridge {

    // @formatter:off
    @Shadow @Final @Nullable private Optional<ResourceLocation> randomSequence;
    @Shadow @Final private static Logger LOGGER;
    @Shadow protected abstract ObjectArrayList<ItemStack> getRandomItems(LootContext p_230923_);
    @Shadow protected abstract List<Integer> getAvailableSlots(Container p_230920_, RandomSource p_230921_);
    @Shadow protected abstract void shuffleAndSplitItems(ObjectArrayList<ItemStack> p_230925_, int p_230926_, RandomSource p_230927_);
    // @formatter:on

    @Unique public CraftLootTable craftLootTable;

    @Override
    public void bridge$setCraftLootTable(CraftLootTable lootTable) {
        this.craftLootTable = lootTable;
    }

    @Override
    public CraftLootTable bridge$getCraftLootTable() {
        return this.craftLootTable;
    }

    @SuppressWarnings("unchecked")
    @Decorate(method = "fill", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    private ObjectArrayList<ItemStack> arclight$nonPluginEvent(LootTable lootTable, LootContext context, Container inv) throws Throwable {
        ObjectArrayList<ItemStack> list = (ObjectArrayList<ItemStack>) DecorationOps.callsite().invoke(lootTable, context);
        if (!context.hasParam(LootContextParams.ORIGIN) && !context.hasParam(LootContextParams.THIS_ENTITY)) {
            return list;
        }
        if (((LootTableBridge) this).bridge$getCraftLootTable() == null) {
            return list;
        }
        LootGenerateEvent event = CraftEventFactory.callLootGenerateEvent(inv, (LootTable) (Object) this, context, list, false);
        if (event.isCancelled()) {
            return (ObjectArrayList<ItemStack>) DecorationOps.cancel().invoke();
        } else {
            return event.getLoot().stream().map(CraftItemStack::asNMSCopy).collect(ObjectArrayList.toList());
        }
    }

    public void fillInventory(Container inv, LootParams lootparams, long i, boolean plugin) {
        LootContext context = (new LootContext.Builder(lootparams)).withOptionalRandomSeed(i).create(this.randomSequence);
        ObjectArrayList<ItemStack> objectarraylist = this.getRandomItems(context);
        RandomSource randomsource = context.getRandom();
        if (((LootTableBridge) this).bridge$getCraftLootTable() != null) {
            LootGenerateEvent event = CraftEventFactory.callLootGenerateEvent(inv, (LootTable) (Object) this, context, objectarraylist, plugin);
            if (event.isCancelled()) {
                return;
            }
            objectarraylist = event.getLoot().stream().map(CraftItemStack::asNMSCopy).collect(ObjectArrayList.toList());
        }

        List<Integer> list = this.getAvailableSlots(inv, randomsource);
        this.shuffleAndSplitItems(objectarraylist, list.size(), randomsource);

        for (ItemStack itemstack : objectarraylist) {
            if (list.isEmpty()) {
                LOGGER.warn("Tried to over-fill a container");
                return;
            }

            if (itemstack.isEmpty()) {
                inv.setItem(list.removeLast(), ItemStack.EMPTY);
            } else {
                inv.setItem(list.removeLast(), itemstack);
            }
        }
    }
}
