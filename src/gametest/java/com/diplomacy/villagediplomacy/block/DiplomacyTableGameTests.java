package com.diplomacy.villagediplomacy.block;

import com.diplomacy.villagediplomacy.VillageDiplomacyMod;
import com.diplomacy.villagediplomacy.core.ModBlocks;
import com.diplomacy.villagediplomacy.core.ModItems;
import com.diplomacy.villagediplomacy.diplomacy.Settlement;
import com.diplomacy.villagediplomacy.diplomacy.SettlementSavedData;
import com.diplomacy.villagediplomacy.menu.DiplomacyTableMenu;
import com.diplomacy.villagediplomacy.menu.RouteMenu;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(VillageDiplomacyMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DiplomacyTableGameTests {
    @GameTest(template = "guard_arena", batch = "diplomacy_table")
    public static void registeredTableOpensAndRoutesTabShowsDiscoveredSettlement(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "TableLeader"));
        var table = (DiplomacyTableBlock) ModBlocks.DIPLOMACY_TABLE.get();
        var state = table.defaultBlockState();
        BlockPos tablePos = helper.absolutePos(new BlockPos(3, 2, 3));
        BlockPos bellPos = tablePos.east(3);
        level.setBlockAndUpdate(bellPos, Blocks.BELL.defaultBlockState());
        player.setPos(Vec3.atBottomCenterOf(tablePos.north()));
        var data = SettlementSavedData.get(level);
        var home = new Settlement(UUID.randomUUID(), "Test home", bellPos, 16);
        home.setLeaderUuid(player.getUUID());
        data.registerSettlement(home);
        var target = new Settlement(UUID.randomUUID(), "Known neighbor", bellPos.south(40), 16);
        data.registerSettlement(target);
        data.discoverSettlement(home.getId(), target.getId());
        try {
            level.setBlockAndUpdate(tablePos, state);
            table.setPlacedBy(level, tablePos, state, player, new ItemStack(ModItems.DIPLOMACY_TABLE.get()));
            helper.assertTrue(level.getBlockState(tablePos).is(table), "Table should remain placed near the bell");
            table.use(state, level, tablePos, player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(tablePos), Direction.UP, tablePos, false));
            helper.assertTrue(player.containerMenu instanceof DiplomacyTableMenu, "Right-click must open the diplomacy menu");
            var menu = (DiplomacyTableMenu) player.containerMenu;
            helper.assertTrue(menu.getSnapshot().canManage(), "Claimed settlement must grant leader access");
            helper.assertTrue(menu.clickMenuButton(player, 6_000), "The Routes action must open its menu");
            helper.assertTrue(player.containerMenu instanceof RouteMenu, "Routes menu must replace the diplomacy menu");
            var routes = (RouteMenu) player.containerMenu;
            helper.assertTrue(routes.snapshot().targets().stream().anyMatch(item -> item.id().equals(target.getId())),
                    "The discovered settlement must appear in Routes");
        } finally {
            player.closeContainer();
            data.removeSettlement(target.getId());
            data.removeSettlement(home.getId());
        }
        helper.succeed();
    }
}
