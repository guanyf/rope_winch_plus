package com.rope_winch_plus.content;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * 绳索绞盘plus 物品（BlockItem 的子类）。
 *
 * <p>相比直接用 {@link BlockItem}，只在悬浮提示里补一句本方块的差异说明
 * （松弛即停、收绳照常），方便玩家理解行为，不改动任何功能逻辑。</p>
 *
 * <p><b>低耦合说明</b>：只用原版 BlockItem / Item / ItemStack，无第三方依赖。</p>
 */
public class RopeWinchPlusItem extends BlockItem {

    public RopeWinchPlusItem(final Block block, final Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final Item.TooltipContext context,
                                final List<Component> tooltip, final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("block.rope_winch_plus.rope_winch_plus.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
