package com.rope_winch_plus.registry;

import com.rope_winch_plus.RopeWinchPlusMod;
import com.rope_winch_plus.content.RopeWinchPlusBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 方块实体类型注册表。
 *
 * <p>仿照官方 {@code SimBlockEntityTypes.ROPE_WINCH} 的注册方式：
 * {@code BlockEntityType.Builder.of((pos, state) -> new 实体(type, pos, state), 方块)}。
 * 这里把「方块」换成本 MOD 的 {@code rope_winch_plus}，「实体」换成
 * {@link RopeWinchPlusBlockEntity}。</p>
 */
public class RopeWinchPlusBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, RopeWinchPlusMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RopeWinchPlusBlockEntity>> ROPE_WINCH_PLUS =
            BLOCK_ENTITY_TYPES.register("rope_winch_plus", RopeWinchPlusBlockEntities::createType);

    /**
     * 用静态方法构造 BlockEntityType，避免在字段初始化器 lambda 内直接引用
     * ROPE_WINCH_PLUS 自身（会导致"初始化程序中存在自引用"编译错误）。
     * supplier 内的 ROPE_WINCH_PLUS.get() 在游戏注册阶段才执行，可拿到正确 type。
     */
    private static BlockEntityType<RopeWinchPlusBlockEntity> createType() {
        return BlockEntityType.Builder.of(
                        (pos, state) -> new RopeWinchPlusBlockEntity(ROPE_WINCH_PLUS.get(), pos, state),
                        RopeWinchPlusBlocks.ROPE_WINCH_PLUS.get())
                .build(null);
    }

    public static void register(final IEventBus bus) {
        BLOCK_ENTITY_TYPES.register(bus);
    }
}
