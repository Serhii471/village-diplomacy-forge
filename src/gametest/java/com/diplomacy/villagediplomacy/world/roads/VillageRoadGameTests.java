package com.diplomacy.villagediplomacy.world.roads;

import com.diplomacy.villagediplomacy.VillageDiplomacyMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.structures.BuriedTreasurePieces;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(VillageDiplomacyMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VillageRoadGameTests {
    @GameTest(template = "guard_arena", batch = "generated_roads", timeoutTicks = 400)
    public static void villageStartsBuildRoadOnBothBanksWithoutFillingWater(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(4, 5, 25));
        // Start on the planner's eight-block sample grid. The water strip then
        // falls between two survey columns, so this tests the paving check even
        // when the arena's absolute position changes with the test order.
        BlockPos aligned = new BlockPos(Math.floorDiv(origin.getX(), 8) * 8 + 4, origin.getY(),
                Math.floorDiv(origin.getZ(), 8) * 8 + 4);
        int surfaceY = level.getMinBuildHeight();
        for (BlockPos pos : BlockPos.betweenClosed(aligned.north(6), aligned.east(40).south(6))) {
            surfaceY = Math.max(surfaceY, level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()));
        }
        BlockPos west = aligned.atY(surfaceY + 1);
        BlockPos east = west.east(40);
        // Build the whole fixture above the generated terrain so hills cannot
        // cover either bank. Add two tagged village starts to exercise discovery.
        for (BlockPos pos : BlockPos.betweenClosed(west.north(6), east.south(6))) {
            Block block = pos.getX() >= west.getX() + 18 && pos.getX() <= west.getX() + 21
                    ? Blocks.WATER : Blocks.GRASS_BLOCK;
            level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        var plainsVillage = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .get(ResourceLocation.fromNamespaceAndPath("minecraft", "village_plains"));
        helper.assertTrue(plainsVillage != null, "Vanilla village structure must exist");
        // Natural villages can be discovered while preparing this disposable world.
        // Start this scenario with an empty queue and no in-progress road search.
        VillageRoads.onLevelUnload(new LevelEvent.Unload(level));
        level.getDataStorage().set(VillageRoadData.STORAGE_KEY, new VillageRoadData());
        addVillageStart(level, west, plainsVillage);
        addVillageStart(level, east, plainsVillage);

        helper.succeedWhen(() -> {
            helper.assertTrue(hasPaving(level, west.east(7).north(3), west.east(17).south(4)),
                    "The road must be built before the water");
            helper.assertTrue(hasPaving(level, west.east(22).north(3), east.west(6).south(4)),
                    "The road must resume on the far bank");
            for (BlockPos pos : BlockPos.betweenClosed(west.east(18).north(6), west.east(21).south(6))) {
                helper.assertTrue(level.getBlockState(pos).is(Blocks.WATER), "Road must leave the water intact");
            }
        });
    }

    @GameTest(template = "guard_arena", batch = "generated_road_sign")
    public static void mountainSignShowsPassAndCompassDirection(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos road = helper.absolutePos(new BlockPos(5, 3, 5));
        BlockPos shoulder = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, road.south()).below();
        level.setBlockAndUpdate(shoulder, Blocks.GRASS_BLOCK.defaultBlockState());
        var chunk = level.getChunkSource().getChunkNow(shoulder.getX() >> 4, shoulder.getZ() >> 4);
        helper.assertTrue(chunk != null, "The sign fixture's chunk must be available");
        int surface = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, shoulder.getX(), shoulder.getZ());
        helper.assertTrue(surface == shoulder.getY(), "Expected surface " + shoulder.getY() + ", got " + surface);
        helper.assertTrue(level.getBlockState(shoulder).is(BlockTags.DIRT), "The sign fixture must stand on soil");
        VillageRoads.placeSign(level, road, 2);
        helper.assertTrue(level.getBlockEntity(shoulder.above()) instanceof SignBlockEntity, "Pass sign must be placed");
        var sign = (SignBlockEntity) level.getBlockEntity(shoulder.above());
        String warning = sign.getFrontText().getMessage(0, false).getString() + " "
                + sign.getFrontText().getMessage(1, false).getString() + " "
                + sign.getFrontText().getMessage(2, false).getString();
        helper.assertTrue(warning.equals("Потрібно перейти гірський перевал"), "Sign must identify the mountain pass");
        helper.assertTrue(sign.getFrontText().getMessage(3, false).getString().equals("Напрямок: Сх"),
                "An eastbound sign must point east");
        helper.succeed();
    }

    private static void addVillageStart(ServerLevel level, BlockPos pos, Structure structure) {
        var chunk = level.getChunkAt(pos);
        var piece = new BuriedTreasurePieces.BuriedTreasurePiece(pos);
        var start = new StructureStart(structure, chunk.getPos(), 0, new PiecesContainer(List.of(piece)));
        chunk.setStartForStructure(structure, start);
        VillageRoads.discover(level, chunk);
    }

    private static boolean hasPaving(ServerLevel level, BlockPos from, BlockPos to) {
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            var block = level.getBlockState(pos);
            if (block.is(Blocks.DIRT_PATH) || block.is(Blocks.COARSE_DIRT) || block.is(Blocks.GRAVEL)) return true;
        }
        return false;
    }
}
