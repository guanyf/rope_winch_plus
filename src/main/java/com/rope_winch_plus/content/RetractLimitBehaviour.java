package com.rope_winch_plus.content;

import com.rope_winch_plus.registry.RopeWinchPlusConfig;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import dev.simulated_team.simulated.index.SimTags;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/**
 * 「收绳上限」数值设置行为（0–64）。
 *
 * <h3>为什么继承 Create 的 ScrollValueBehaviour</h3>
 * 用户要求复用 Create 面板，而 {@link ScrollValueBehaviour} 已经内置了：
 * <ul>
 *   <li><b>NBT 读写</b>（{@code write}/{@code read}）→ 数值自动随方块存档、跨重启保留；</li>
 *   <li><b>数值面板</b>（{@code createBoard} → Create 的 {@code ValueSettingsScreen}）；</li>
 *   <li><b>网络同步</b>（Create 的 {@code ValueSettingsPacket}）；</li>
 *   <li><b>世界内数值框</b>（悬浮数字，直接显示当前设定值）。</li>
 * </ul>
 * 本类<b>只追加两处闸门</b>，其余 100% 交给 Create，符合低耦合要求：
 *
 * <h3>闸门一：只允许「剩余的两个面」</h3>
 * 绞盘 6 个面的分工：
 * <pre>
 *   放绳面(前)   = FACING
 *   底座吸墙面(后)= FACING.getOpposite()
 *   应力输入面×2 = 转轴方向 (getRotationAxis)
 *   剩余 2 面     = 与上述两根轴都垂直的第三根轴 —— 本功能的目标面
 * </pre>
 * 实现方式是给 {@link CenteredSideValueBoxTransform} 传一个
 * {@code BiPredicate<BlockState, Direction>}：Create 的
 * {@code ValueSettingsInputHandler.onBlockActivated} 在处理右键时会调用
 * {@code slotPositioning.isSideActive(state, 被点的面)}，不满足就跳过该行为。
 * 判面逻辑见 {@link #isSettingFace(BlockState, Direction)}。
 *
 * <h3>闸门二：剪绳物品必须放行</h3>
 * Create 一旦接受这次右键就会 {@code event.setCanceled(true)}，方块自己的
 * {@code useItemOn} 便不再执行——那剪绳就剪不掉了。因此
 * {@link #mayInteract(Player)} 对航空学的 {@code DESTROYS_ROPE} 物品返回 false，
 * 让剪绳操作正常走官方逻辑。
 *
 * <p>另注：{@link #testHit(Vec3)} 恒返回 true，因为「点在哪」已由上面的判面闸门
 * 决定；否则 Create 默认只认数值框那一小块命中区，点面空白处不会弹面板。</p>
 */
public class RetractLimitBehaviour extends ScrollValueBehaviour {

    /** 收绳上限最小值：0 = 收绳到顶（与未加本功能时一致）。 */
    public static final int MIN = 0;
    /** 收绳上限最大值。 */
    public static final int MAX = 64;

    public RetractLimitBehaviour(final SmartBlockEntity blockEntity) {
        super(Component.translatable("rope_winch_plus.retract_limit"),
                blockEntity,
                new CenteredSideValueBoxTransform(RetractLimitBehaviour::isSettingFace));

        this.between(MIN, MAX);

        // 新放下的方块取配置文件里的默认值；已存在的方块随后会被 NBT 中的存档值覆盖。
        this.value = RopeWinchPlusConfig.DEFAULT_RETRACT_LIMIT.get();

        // 数值变更后立刻标记脏，确保写入存档。
        this.withCallback(value -> blockEntity.setChanged());
    }

    /**
     * 判断某个面是否是「可右键设置」的那两个面。
     *
     * <p>取与「FACING 所在轴」和「转轴」都垂直的第三根轴，该轴上的两个面即目标面。</p>
     *
     * @param state 绞盘当前方块状态
     * @param face  待判定的面
     * @return true 表示该面可以右键弹出设置面板
     */
    public static boolean isSettingFace(final BlockState state, final Direction face) {
        if (!(state.getBlock() instanceof final DirectionalAxisKineticBlock kineticBlock)) {
            return false;
        }

        final Direction.Axis facingAxis = state.getValue(BlockStateProperties.FACING).getAxis();
        final Direction.Axis rotationAxis = kineticBlock.getRotationAxis(state);

        for (final Direction.Axis axis : Direction.Axis.values()) {
            if (axis != facingAxis && axis != rotationAxis) {
                return face.getAxis() == axis;
            }
        }

        return false;
    }

    /**
     * 命中判定：判面已由 {@code CenteredSideValueBoxTransform} 的谓词完成，
     * 这里放行整面点击，避免只有数值框那一小块可点。
     */
    @Override
    public boolean testHit(final Vec3 hit) {
        return true;
    }

    /**
     * 手持剪绳类物品时不接管右键，交还给官方逻辑去剪绳。
     */
    @Override
    public boolean mayInteract(final Player player) {
        return !player.getMainHandItem().is(SimTags.Items.DESTROYS_ROPE);
    }
}
