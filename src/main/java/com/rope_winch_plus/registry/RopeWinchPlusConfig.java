package com.rope_winch_plus.registry;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 绳索绞盘plus 的可调参数（ForgeConfigSpec，改完重启游戏即生效，无需重新编译）。
 *
 * <p>游戏首次启动后会在 游戏目录/config/rope_winch_plus-server.toml 自动生成配置文件，
 * 直接编辑其中的数值即可。</p>
 *
 * <p>读取方式：{@code RopeWinchPlusConfig.MAX_ROPE_SLACK_ALLOWED.get()}。</p>
 *
 * <p><b>低耦合说明</b>：只用了 NeoForge 自带的 {@link ModConfigSpec}，
 * 未依赖任何其它模组的配置框架。</p>
 */
public final class RopeWinchPlusConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /**
     * 绳索松弛容差（%）：当绳索两端直线距离 &lt; 名义长度 × (1 − 该值/100) 时判定为松弛，
     * 停止继续放绳（只允许收绳）。
     *
     * <p>调大 → 更早停放（对松弛更敏感）；调小 → 更晚停放（更松才停）；0 → 关闭松弛停放。</p>
     */
    public static final ModConfigSpec.IntValue MAX_ROPE_SLACK_ALLOWED;

    /**
     * 新放下的「绳索绞盘plus」的默认收绳上限（0–64）。
     *
     * <p>语义：0 = 收绳到顶（与未加本功能时一致）；N = 收绳时在到顶基础上再多留 N 格绳长。
     * 注意这只决定<b>新放下方块的初始值</b>，已放置的方块的数值以它自身保存的为准，
     * 玩家可右键方块单独修改，不受此配置影响。</p>
     */
    public static final ModConfigSpec.IntValue DEFAULT_RETRACT_LIMIT;

    /**
     * 绳索收到底（到顶）时的「对齐余量」（格）。
     *
     * <p>官方绳索的硬下限是 1.0 格，但原版绞盘收到底时视觉上会<b>多收约 1/4 格</b>
     * （绳端比方块表面对齐线多缩一条纹），导致绳端无法与方块完美对齐。本 MOD 在官方
     * 1.0 格基础上再<b>多留 {该值} 格</b>，使收绳下限 = {@code 1.0 + 该值 + N}，
     * 从而实现绳端与方块对齐。</p>
     *
     * <p>该值允许微调：调大 → 更早停（绳端离方块更远）；调小 → 更晚停（更贴近方块）。
     * 默认 0.18（用户实测对齐最准的值）。实测若发现偏差，可改成 0.15、0.20、0.30 等试到对齐为止。</p>
     */
    public static final ModConfigSpec.DoubleValue ROPE_ALIGN_EXTRA;

    /** 向 NeoForge 注册用的完整 SPEC（在 RopeWinchPlusMod 中 registerConfig）。 */
    public static final ModConfigSpec SPEC;

    static {
        MAX_ROPE_SLACK_ALLOWED = BUILDER
                .comment("绳索松弛容差(%)：两端直线距离 < 名义长度*(1-该值/100) 时判定松弛并停止放绳(只允许收绳)。0=关闭。")
                .defineInRange("maxRopeSlackAllowed", 15, 0, 95);

        DEFAULT_RETRACT_LIMIT = BUILDER
                .comment("新放下的绳索绞盘plus 的默认收绳上限(0-64)。0=收绳到顶；N=到顶再多留N格绳长。仅影响新放置的方块。")
                .defineInRange("defaultRetractLimit", 0, 0, 64);

        ROPE_ALIGN_EXTRA = BUILDER
                .comment("绳索收到底时的对齐余量(格)。官方硬下限为1.0格，但原版收到底视觉上多收约1/4格，绳端无法对齐方块。")
                .comment("本值在1.0之上再多留该格数：收绳下限 = 1.0 + 该值 + 收绳上限N。调大=更早停(绳端离方块远)，调小=更贴近方块。")
                .comment("默认0.18(用户实测对齐最准的值)。若想微调对齐，可改成0.15/0.20/0.30等。")
                .defineInRange("ropeAlignExtra", 0.18, 0.0, 1.0);

        SPEC = BUILDER.build();
    }

    private RopeWinchPlusConfig() {}
}
