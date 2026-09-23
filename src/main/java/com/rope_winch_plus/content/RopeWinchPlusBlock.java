package com.rope_winch_plus.content;

import com.rope_winch_plus.registry.RopeWinchPlusBlockEntities;
import dev.simulated_team.simulated.content.blocks.rope.rope_winch.RopeWinchBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 绳索绞盘plus 方块。
 *
 * <p><b>设计原则（低耦合）：本方块只继承航空学 MOD（simulated）的
 * {@link RopeWinchBlock}，把原版绞盘的出绳、连接、扳手转向、剪绳、放置朝向等
 * 全部逻辑 100% 继承官方实现，本类唯一职责是把方块映射到本 MOD 自定义的
 * 方块实体类型（{@link RopeWinchPlusBlockEntity}），从而让松弛停放逻辑生效。</p>
 *
 * <p>相比旧版（平行重写出绳/tick/渲染，导致抖动、看不到绳、不动）的教训，
 * 这里不再自己实现任何绳索逻辑，只在 {@link RopeWinchPlusBlockEntity#getMovementSpeed()}
 * 一个虚方法上注入「松弛即停放绳」。</p>
 */
public class RopeWinchPlusBlock extends RopeWinchBlock {

    public RopeWinchPlusBlock(final BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * 把本方块映射到本 MOD 自定义的方块实体类型。
     *
     * <p>覆写的返回类型 {@code BlockEntityType<? extends RopeWinchPlusBlockEntity>}
     * 是父类 {@code BlockEntityType<? extends RopeWinchBlockEntity>} 的协变（covariant）收窄，
     * 合法（{@code RopeWinchPlusBlockEntity} 是 {@code RopeWinchBlockEntity} 的子类）。</p>
     *
     * <p><b>注意：这里只覆写 getBlockEntityType()，不覆写 getBlockEntityClass()。</b>
     * 父类的 {@code getBlockEntityClass()} 返回具体类型 {@code Class<RopeWinchBlockEntity>}，
     * 泛型是<b>不变（invariant）</b>的，若覆写成 {@code Class<RopeWinchPlusBlockEntity>} 会编译报错；
     * 而且实际创建方块实体用的是 {@code BlockEntityType.Builder.of((pos,state)->new 实体(...), 方块)}
     * 里的构造器 lambda（见 {@link RopeWinchPlusBlockEntities}），所以本类无需提供 getBlockEntityClass()。</p>
     */
    @Override
    public BlockEntityType<? extends RopeWinchPlusBlockEntity> getBlockEntityType() {
        return RopeWinchPlusBlockEntities.ROPE_WINCH_PLUS.get();
    }
}
