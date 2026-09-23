package com.rope_winch_plus.content;

import com.rope_winch_plus.registry.RopeWinchPlusConfig;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.simulated_team.simulated.content.blocks.rope.rope_winch.RopeWinchBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerRopeStrand;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;

import java.util.List;

/**
 * 绳索绞盘plus 方块实体。
 *
 * <p><b>设计原则（低耦合）：</b>继承航空学 MOD（simulated）的
 * {@link RopeWinchBlockEntity}，出绳 / 连接 / 剪绳 / 扳手 / 渲染 / 阈值开关读数
 * 等全部逻辑 100% 继承官方。本类只追加两件事：</p>
 * <ol>
 *   <li>重写 {@link #getMovementSpeed()} —— 注入两个「自动停」策略（见下）；</li>
 *   <li>挂载 {@link RetractLimitBehaviour} —— 收绳上限的数值设置（0–64）。</li>
 * </ol>
 *
 * <h3>为什么只改 getMovementSpeed() 就够</h3>
 * 官方 {@code RopeWinchBlockEntity} 的私有方法 {@code updateRopeStrandExtension(strand)}
 * 每 tick 用
 * <pre>
 *   float movementSpeed = this.getMovementSpeed();   // ← invokevirtual 虚调用
 *   ... 在 movementSpeed 上做「过紧停止」「超最大长度停止」等钳制 ...
 *   extension += movementSpeed;                      // 推进绳索长度
 * </pre>
 * 的方式驱动绳索。因为这是<b>虚方法调用</b>，本类重写后，官方每 tick 取到的
 * {@code movementSpeed} 就是本类的返回值。两个策略因此都能生效，且完全不碰官方内部实现。
 *
 * <h3>功能一：松弛停放绳（放绳方向，movementSpeed &gt; 0）</h3>
 * <p>关键：物理绳松了只下垂堆叠，<b>弧长几乎不变</b>，所以拿弧长判松弛永远不触发
 * （旧版判据方向就错在这）。正确的松弛信号是<b>两端直线距离明显小于绳的名义长度</b>。</p>
 * <p>判据：{@code straightDistance < ropeLength × (1 − slack%/100)} 时返回 0，停止放绳。
 * 阈值由 {@link RopeWinchPlusConfig#MAX_ROPE_SLACK_ALLOWED} 配置（默认 15%）。</p>
 *
 * <h3>功能二：收绳限位（收绳方向，movementSpeed &lt; 0）</h3>
 * <p>玩家可为<b>每个绞盘</b>单独设置收绳上限 N（0–64），收绳到该长度即停。</p>
 * <p><b>关键实现细节：</b>官方绳索的硬下限是 {@value #BASE_ROPE_LENGTH} 格，但原版收到底时
 * <b>视觉上多收约 1/4 格</b>（绳端比方块表面对齐线多缩一条纹）。为对齐方块，plus 在硬下限之上
 * 再多留 {@code ROPE_ALIGN_EXTRA} 格（默认 0.18，可在 config 调，见
 * {@link RopeWinchPlusConfig#ROPE_ALIGN_EXTRA}）。按用户确认的语义，N 表示「在到顶基础上再多留
 * N 格」，故实际停下来的绳长下限 = {@code BASE_ROPE_LENGTH + ROPE_ALIGN_EXTRA + N}，
 * 即比原版少收 ROPE_ALIGN_EXTRA 格；若按绝对值理解，0 和 1 会停在同一点、第 1 档就失效了。</p>
 * <p>为避免一帧冲过头，当 {@code ropeLength + movementSpeed < 下限} 时返回
 * {@code 下限 − ropeLength}，让绳索<b>精确停在限位</b>上。</p>
 *
 * <p>两个策略一正一负、互不干扰：正转放绳遇松弛自动停，反转收绳到限位自动停。</p>
 *
 * <p><b>低耦合说明</b>：本类只依赖 simulated（绳索绞盘/绳索类）、Create
 * （{@link BlockEntityBehaviour} 行为基类）与 MC 原生，没有从别处搬运第三方代码。</p>
 */
public class RopeWinchPlusBlockEntity extends RopeWinchBlockEntity {

    /**
     * 官方绳索的硬下限（格）。绳索不可能短于 1.0 格（官方在点数降到最少时把 extension 钳到 1.0）。
     *
     * <p>plus 在对齐方块时，会在该硬下限之上再<b>多留 {@code ROPE_ALIGN_EXTRA} 格</b>
     * （默认 0.18，见 {@link RopeWinchPlusConfig#ROPE_ALIGN_EXTRA}），因为原版收到底时
     * 视觉上多收约 1/4 格、绳端无法对齐方块。收绳下限 =
     * {@code 1.0 + ROPE_ALIGN_EXTRA + N}，比原版少收 ROPE_ALIGN_EXTRA 格。</p>
     *
     * <p>注：绳索 extension 支持小数（官方以浮点累加 movementSpeed），
     * 停在 1.25 这种非整格长度没有任何问题。</p>
     */
    private static final double BASE_ROPE_LENGTH = 1.0;

    /** 收绳上限数值设置行为（0–64）。构造完成前为 null，故 {@link #getRetractLimit()} 需判空。 */
    private RetractLimitBehaviour retractLimit;

    public RopeWinchPlusBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    /**
     * 追加「收绳上限」行为。
     *
     * <p>{@code super.addBehaviours} 会先加入官方的绳索持有行为，本行为排在后面，
     * 与官方行为完全解耦。NBT 读写由 Create 的 {@code SmartBlockEntity} 统一遍历行为处理，
     * 无需本类干预。</p>
     */
    @Override
    public void addBehaviours(final List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(this.retractLimit = new RetractLimitBehaviour(this));
    }

    /**
     * @return 本方块当前设置的收绳上限（0–64）；行为尚未构造好时返回 0。
     */
    public int getRetractLimit() {
        return this.retractLimit == null ? 0 : this.retractLimit.getValue();
    }

    /**
     * 绳索收放速度（方块/tick）。
     *
     * <p>在官方返回值（已把转速换算为线速度并钳到 ±0.49）的基础上，追加两个「自动停」：
     * <ul>
     *   <li>{@code movementSpeed < 0}（收绳）：到达收绳下限则停，见 {@link #clampRetract}；</li>
     *   <li>{@code movementSpeed > 0}（放绳）：绳索已足够松弛则停。</li>
     * </ul>
     * 其余情形（速度为 0、无绳、客户端）一律原样返回。</p>
     */
    @Override
    public float getMovementSpeed() {
        final float movementSpeed = super.getMovementSpeed();

        // 只在「服务器端 + 有绳」时才需要介入：
        //  - 客户端没有 ServerRopeStrand，且本方法在客户端 tick 也会被调（驱动表盘动画），必须跳过。
        if (this.level == null || this.level.isClientSide) {
            return movementSpeed;
        }

        final ServerRopeStrand strand = this.getRopeHolder().getOwnedStrand();
        if (strand == null) {
            return movementSpeed;
        }

        final ObjectList<Vector3d> pts = strand.getPoints();
        if (pts == null || pts.size() < 2) {
            return movementSpeed;
        }

        // 名义绳长：与官方 updateRopeStrandExtension 里 desiredExtension 的算法一致。
        final double ropeLength = strand.getExtension() + (pts.size() - 2) * ServerRopeStrand.SEGMENT_LENGTH;
        if (ropeLength <= 0.0) {
            return movementSpeed;
        }

        /* ===== 功能二：收绳限位（反转收绳） ===== */
        if (movementSpeed < 0.0f) {
            return this.clampRetract(movementSpeed, ropeLength);
        }

        /* ===== 功能一：松弛停放绳（正转放绳） ===== */
        if (movementSpeed > 0.0f) {
            // 两端直线距离：points[0]=绞盘端，points[last]=远端（另一端）。
            final double straightDistance = pts.get(0).distance(pts.get(pts.size() - 1));
            final double slackThreshold = RopeWinchPlusConfig.MAX_ROPE_SLACK_ALLOWED.get() / 100.0;

            if (straightDistance < ropeLength * (1.0 - slackThreshold)) {
                return 0.0f;
            }
        }

        return movementSpeed;
    }

    /**
     * 收绳限位：把收绳速度钳到「不会短于下限」。
     *
     * @param movementSpeed 官方算出的收绳速度（负值）
     * @param ropeLength    当前名义绳长
     * @return 钳制后的速度；已在限位处则为 0
     */
    private float clampRetract(final float movementSpeed, final double ropeLength) {
        // 下限 = 官方硬下限(1.0) + 对齐余量(可配,默认0.18) + 收绳上限 N。
        // 「到顶」已剩 BASE_ROPE_LENGTH，N 表示在此基础上再多留 N 格。
        final double minLength = BASE_ROPE_LENGTH + RopeWinchPlusConfig.ROPE_ALIGN_EXTRA.get() + this.getRetractLimit();

        // 已经到达（或短于）限位 → 停止收绳。
        if (ropeLength <= minLength) {
            return 0.0f;
        }

        // 这一 tick 会冲过限位 → 只走到限位为止，精确停在限位上，不会来回抖动。
        if (ropeLength + movementSpeed < minLength) {
            return (float) (minLength - ropeLength);
        }

        return movementSpeed;
    }
}
