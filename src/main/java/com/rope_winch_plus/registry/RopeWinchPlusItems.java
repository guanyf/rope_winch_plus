package com.rope_winch_plus.registry;

import com.rope_winch_plus.RopeWinchPlusMod;
import com.rope_winch_plus.content.RopeWinchPlusItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 物品注册表。方块必须配套 BlockItem，否则创造模式物品栏与配方都拿不到。
 *
 * <p><b>低耦合说明</b>：只用 NeoForge 自带的 {@link DeferredRegister}。</p>
 */
public class RopeWinchPlusItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.ITEM, RopeWinchPlusMod.MODID);

    public static final DeferredHolder<Item, RopeWinchPlusItem> ROPE_WINCH_PLUS = ITEMS.register(
            "rope_winch_plus",
            () -> new RopeWinchPlusItem(RopeWinchPlusBlocks.ROPE_WINCH_PLUS.get(),
                    new Item.Properties()));

    public static void register(final IEventBus bus) {
        ITEMS.register(bus);
    }
}
