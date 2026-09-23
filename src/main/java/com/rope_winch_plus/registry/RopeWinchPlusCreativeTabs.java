package com.rope_winch_plus.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 绳索绞盘plus 的<b>独立创造分组</b>。
 *
 * <p>按用户要求：方块放进<b>新建的独立分组</b>，不塞进机械动力 / 航空学的任何既有分组
 * （旧版塞进 create:base / 物理模拟 分区被明确否决）。</p>
 *
 * <p>分组标题：{@code itemGroup.rope_winch_plus}（中文"绳索绞盘plus"）；
 * 图标：绳索绞盘plus 方块本身。</p>
 *
 * <p><b>低耦合说明</b>：只用 NeoForge 的 {@link DeferredRegister} + 原版
 * {@link CreativeModeTab}，不依赖 Registrate。</p>
 */
public class RopeWinchPlusCreativeTabs {

    /**
     * 在给定 DeferredRegister（由 Mod 入口创建并绑定到 modEventBus）上注册本 MOD 的独立分组。
     */
    public static void register(final DeferredRegister<CreativeModeTab> tabs) {
        tabs.register("winch_plus", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.rope_winch_plus"))
                .icon(() -> new ItemStack(RopeWinchPlusItems.ROPE_WINCH_PLUS.get()))
                .displayItems((params, output) -> {
                    output.accept(RopeWinchPlusItems.ROPE_WINCH_PLUS.get());
                })
                .build());
    }

    private RopeWinchPlusCreativeTabs() {}
}
