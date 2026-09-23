package com.rope_winch_plus;

import com.rope_winch_plus.registry.RopeWinchPlusBlocks;
import com.rope_winch_plus.registry.RopeWinchPlusBlockEntities;
import com.rope_winch_plus.registry.RopeWinchPlusConfig;
import com.rope_winch_plus.registry.RopeWinchPlusCreativeTabs;
import com.rope_winch_plus.registry.RopeWinchPlusItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 绳索绞盘plus —— Create Aeronautics(航空学/物理模拟) 附属 MOD 入口。
 *
 * <h3>功能</h3>
 * 提供一个方块「绳索绞盘plus」，与航空学 MOD 的绳索绞盘<b>功能一致</b>
 * （接动力网络：正转放绳、反转收绳、可连接/剪绳/扳手转向），额外增加：
 * <b>当绳索比较松弛时自动停止继续放绳</b>（只停放绳，收绳始终有效），
 * 方便下放到位后不用人工关停、反转即可直接收绳。
 *
 * <h3>设计（低耦合）</h3>
 * 继承航空学 MOD（simulated）的 {@code RopeWinchBlock} / {@code RopeWinchBlockEntity}，
 * 只在 {@code getMovementSpeed()} 一个虚方法上注入松弛停放逻辑；
 * 出绳 / 连接 / 渲染 / 扳手 / 剪绳全部继承官方，不平行重写。
 * 运行时 API 依赖只有 simulated；create / sable / neoforge 仅为编译其父类所需。
 *
 * <h3>分组</h3>
 * 物品放进<b>本 MOD 独立的新分组</b>（见 {@link RopeWinchPlusCreativeTabs}），
 * 不塞进机械动力 / 航空学任何既有分组。
 */
@Mod(RopeWinchPlusMod.MODID)
public class RopeWinchPlusMod {
    public static final String MODID = "rope_winch_plus";

    public RopeWinchPlusMod(final IEventBus modEventBus, final ModContainer modContainer) {
        RopeWinchPlusBlocks.register(modEventBus);
        RopeWinchPlusItems.register(modEventBus);
        RopeWinchPlusBlockEntities.register(modEventBus);

        // 独立创造分组（新分组，不复用其它组）。
        DeferredRegister<CreativeModeTab> TABS =
                DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
        TABS.register(modEventBus);
        RopeWinchPlusCreativeTabs.register(TABS);

        // 注册配置文件：游戏启动后在 游戏目录/config/rope_winch_plus-server.toml 生成。
        modContainer.registerConfig(ModConfig.Type.SERVER, RopeWinchPlusConfig.SPEC);
    }
}
