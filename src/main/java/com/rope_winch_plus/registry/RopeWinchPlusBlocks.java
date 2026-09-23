package com.rope_winch_plus.registry;

import com.rope_winch_plus.RopeWinchPlusMod;
import com.rope_winch_plus.content.RopeWinchPlusBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 方块注册表。
 *
 * <p><b>低耦合说明</b>：只用 NeoForge 自带的 {@link DeferredRegister}，
 * 不依赖 Registrate / 任何其它模组的注册器。</p>
 *
 * <p>方块属性（硬度/声音/需正确工具掉落）与原版绳索绞盘手感一致：
 * 可被镐/斧挖取、需正确工具才掉落。</p>
 */
public class RopeWinchPlusBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.BLOCK, RopeWinchPlusMod.MODID);

    public static final DeferredHolder<Block, RopeWinchPlusBlock> ROPE_WINCH_PLUS = BLOCKS.register(
            "rope_winch_plus",
            () -> new RopeWinchPlusBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.WOOD)
                    .requiresCorrectToolForDrops()));

    public static void register(final IEventBus bus) {
        BLOCKS.register(bus);
    }
}
