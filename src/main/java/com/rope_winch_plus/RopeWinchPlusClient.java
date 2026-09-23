package com.rope_winch_plus;

import com.rope_winch_plus.registry.RopeWinchPlusBlockEntities;
import dev.simulated_team.simulated.content.blocks.rope.rope_winch.RopeWinchRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 客户端专用：把航空学 MOD 原版的 {@link RopeWinchRenderer} 注册到
 * 本 MOD 的方块实体类型上。
 *
 * <p><b>为什么要这么做（延用原版外观 + 能看到绳索）：</b>
 * 原版绳索绞盘的动态外观（旋转的轴、滚动的绳圈、垂下的绳索）不是普通方块模型，
 * 而是由 {@link RopeWinchRenderer} 通过航空学 MOD 内部的 PartialModel
 * （{@code SimPartialModels.ROPE_WINCH_SHAFT / ROPE_WINCH_ROPE_COIL}）+ 贴图位移 +
 * {@code RopeStrandRenderer} 绘制的。本 MOD 继承自官方绞盘，外观/渲染与官方一致，
 * 因此直接<b>复用原版渲染器</b>即可：
 * <ul>
 *   <li>绞盘本体模型：由本方块 blockstate 指向原版 {@code simulated:block/rope_winch/...}；</li>
 *   <li>旋转轴 / 绳圈 / 绳索：由原版 {@link RopeWinchRenderer} 绘制（其引用的是
 *       航空学 MOD 全局注册的资源，不依赖具体是哪一个绞盘方块实体类型）。</li>
 * </ul>
 * 因为 {@link com.rope_winch_plus.content.RopeWinchPlusBlockEntity} 是
 * {@code RopeWinchBlockEntity} 的子类，原版渲染器完全兼容，无需自绘。</p>
 *
 * <p><b>注册方式（正确 API + 服务器安全）：</b>
 * <ul>
 *   <li>用 {@link EventBusSubscriber}（{@code value = Dist.CLIENT}）声明本类，
 *       NeoForge 会<b>只在客户端</b>加载并注册本类的事件监听器——专用服务器不会加载
 *       这个引用了客户端渲染类的文件，避免服务器端 ClassNotFound；</li>
 *   <li>NeoForge 1.21.1 中，方块实体渲染器通过<b>原版</b>
 *       {@link BlockEntityRenderers#register} 注册，时机为 {@link FMLClientSetupEvent}
 *       （Registrate 内部就是这么做的，本 MOD 不走 Registrate，直接调原版 API）；</li>
 *   <li>不写已过时的 {@code bus = ...} 属性（{@code @EventBusSubscriber} 默认即 mod bus）。</li>
 * </ul>
 * </p>
 *
 * <p><b>低耦合说明</b>：复用的是航空学 MOD 已公开（public）的渲染器类，
 * 属于"复用依赖 MOD 的公开组件"，没有搬运第三方代码，也没有自绘。</p>
 */
@EventBusSubscriber(modid = RopeWinchPlusMod.MODID, value = Dist.CLIENT)
public class RopeWinchPlusClient {

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        // 原版 API：把本 MOD 方块实体类型映射到航空学 MOD 原版渲染器。
        BlockEntityRenderers.register(
                RopeWinchPlusBlockEntities.ROPE_WINCH_PLUS.get(),
                (BlockEntityRendererProvider.Context ctx) -> new RopeWinchRenderer(ctx));
    }

    private RopeWinchPlusClient() {}
}
