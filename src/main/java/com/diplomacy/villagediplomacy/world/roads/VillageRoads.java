package com.diplomacy.villagediplomacy.world.roads;

import com.diplomacy.villagediplomacy.VillageDiplomacyMod;
import com.diplomacy.villagediplomacy.world.roads.RoadPlanner.Point;
import com.diplomacy.villagediplomacy.world.roads.VillageRoadData.Plot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/** Reads real village starts after generation and builds a little at a time in loaded chunks. */
@Mod.EventBusSubscriber(modid = VillageDiplomacyMod.MOD_ID)
public final class VillageRoads {
    private static final int SEARCH_BUDGET = 2;
    private static final int BUILD_BUDGET = 12;
    private static final int KEEP_CLEAR_AT_VILLAGE = 6;
    private static final String[] COMPASS = {"Пн", "ПнСх", "Сх", "ПдСх", "Пд", "ПдЗх", "Зх", "ПнЗх"};
    private static final int[] DX = {0, 1, 1, 1, 0, -1, -1, -1};
    private static final int[] DZ = {-1, -1, 0, 1, 1, 1, 0, -1};
    private static final Map<ServerLevel, Work> WORLDS = new WeakHashMap<>();

    private static final class Work {
        final Set<Long> loadedWork = new LinkedHashSet<>();
        VillageRoadData.Link link;
        RoadPlanner planner;
    }

    private VillageRoads() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) return;
        ChunkAccess chunk = event.getChunk();
        if (level.getServer().isSameThread()) {
            discover(level, chunk);
        } else {
            level.getServer().execute(() -> discover(level, chunk));
        }
    }

    static void discover(ServerLevel level, ChunkAccess chunk) {
        VillageRoadData data = VillageRoadData.get(level);
        var structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        chunk.getAllStarts().forEach((structure, start) -> {
            if (start.isValid() && structures.getResourceKey(structure).flatMap(structures::getHolder)
                    .filter(holder -> holder.is(StructureTags.VILLAGE)).isPresent()) {
                data.discover(start.getPieces().get(0).getBoundingBox().getCenter());
            }
        });
        long chunkKey = chunk.getPos().toLong();
        if (data.hasPlots(chunkKey)) {
            WORLDS.computeIfAbsent(level, ignored -> new Work()).loadedWork.add(chunkKey);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) WORLDS.remove(level);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD) return;
        VillageRoadData data = VillageRoadData.get(level);
        Work work = WORLDS.computeIfAbsent(level, ignored -> new Work());
        if (work.planner == null && level.getGameTime() % 20 == 0) {
            work.link = data.nextLink();
            if (work.link != null) {
                work.planner = new RoadPlanner(point(work.link.first()), point(work.link.second()),
                        at -> survey(level, at));
            }
        }
        if (work.planner != null && work.planner.advance(SEARCH_BUDGET)) {
            List<RoadPlanner.Segment> route = work.planner.result();
            if (route.isEmpty()) {
                VillageDiplomacyMod.LOGGER.debug("No road found in terrain search between {} and {}",
                        work.link.first(), work.link.second());
            }
            Set<Long> chunks = data.finishPlanning(work.link, plots(route));
            for (long key : chunks) {
                if (loaded(level, key) != null) work.loadedWork.add(key);
            }
            work.planner = null;
        }
        buildLoaded(level, data, work);
    }

    private static void buildLoaded(ServerLevel level, VillageRoadData data, Work work) {
        for (int remaining = BUILD_BUDGET; remaining > 0 && !work.loadedWork.isEmpty(); remaining--) {
            long key = work.loadedWork.iterator().next();
            LevelChunk chunk = loaded(level, key);
            if (chunk != null) {
                Plot plot = data.pollPlot(key);
                if (plot != null) {
                    if (plot.bearing() < 0) pave(level, chunk, plot.column());
                    else placeSign(level, plot.column(), plot.bearing());
                }
            }
            if (chunk == null || !data.hasPlots(key)) work.loadedWork.remove(key);
        }
    }

    private static LevelChunk loaded(ServerLevel level, long key) {
        return level.getChunkSource().getChunkNow(ChunkPos.getX(key), ChunkPos.getZ(key));
    }

    private static RoadPlanner.Terrain survey(ServerLevel level, Point point) {
        int x = point.x(), z = point.z();
        LevelChunk existing = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
        int height;
        BlockState surface;
        if (existing != null) {
            height = existing.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15);
            surface = existing.getBlockState(new BlockPos(x, height, z));
        } else {
            var source = level.getChunkSource();
            var noise = source.getGenerator().getBaseColumn(x, z, level, source.randomState());
            height = level.getMaxBuildHeight() - 1;
            while (height > level.getMinBuildHeight() && noise.getBlock(height).isAir()) height--;
            surface = noise.getBlock(height);
        }
        var biome = level.getUncachedNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(height), QuartPos.fromBlock(z));
        boolean wet = !surface.getFluidState().isEmpty();
        boolean river = wet && biome.is(BiomeTags.IS_RIVER);
        boolean forest = surface.is(BlockTags.LEAVES) || surface.is(BlockTags.LOGS)
                || biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_TAIGA) || biome.is(BiomeTags.IS_JUNGLE);
        return new RoadPlanner.Terrain(height, river, wet && !river, forest, biome.is(BiomeTags.IS_MOUNTAIN));
    }

    private static Point point(BlockPos pos) {
        return new Point(pos.getX(), pos.getZ());
    }

    static List<Plot> plots(List<RoadPlanner.Segment> route) {
        if (route.isEmpty()) return List.of();
        Point start = route.get(0).from();
        Point end = route.get(route.size() - 1).to();
        List<Plot> output = new ArrayList<>();
        Set<Point> paved = new HashSet<>();
        Point passStart = null;
        for (var segment : route) {
            if (segment.pass()) {
                if (passStart == null) passStart = segment.from();
                continue;
            }
            if (passStart != null) {
                Point resume = segment.from();
                if (clearOfVillages(passStart, start, end)) output.add(passSign(passStart, resume));
                if (clearOfVillages(resume, start, end)) output.add(passSign(resume, passStart));
                passStart = null;
            }
            for (Point column : RoadPlanner.raster(segment.from(), segment.to())) {
                if (clearOfVillages(column, start, end) && paved.add(column)) {
                    output.add(new Plot(new BlockPos(column.x(), 0, column.z()), -1));
                }
            }
        }
        return output;
    }

    private static boolean clearOfVillages(Point point, Point start, Point end) {
        return point.distance(start) >= KEEP_CLEAR_AT_VILLAGE && point.distance(end) >= KEEP_CLEAR_AT_VILLAGE;
    }

    private static Plot passSign(Point at, Point resume) {
        double angle = Math.atan2(resume.x() - (double) at.x(), at.z() - (double) resume.z());
        int bearing = Math.floorMod((int) Math.round(angle * 4 / Math.PI), 8);
        return new Plot(new BlockPos(at.x(), 0, at.z()), bearing);
    }

    private static BlockPos surface(LevelChunk chunk, BlockPos column) {
        int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, column.getX() & 15, column.getZ() & 15);
        BlockPos top = new BlockPos(column.getX(), y, column.getZ());
        return shortPlant(chunk.getBlockState(top)) ? top.below() : top;
    }

    static void pave(ServerLevel level, LevelChunk chunk, BlockPos column) {
        BlockPos ground = surface(chunk, column);
        BlockState soil = chunk.getBlockState(ground);
        if (!roadSoil(soil) || !soil.getFluidState().isEmpty()) return;
        BlockPos above = ground.above();
        BlockState cover = chunk.getBlockState(above);
        if (!cover.isAir() && !shortPlant(cover)) return;
        if (soil.is(Blocks.DIRT_PATH) || soil.is(Blocks.COARSE_DIRT) || soil.is(Blocks.GRAVEL)) return;
        if (shortPlant(cover)) level.setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        long hash = column.getX() * 341873128712L + column.getZ() * 132897987541L ^ level.getSeed();
        int style = Math.floorMod((int) (hash ^ (hash >>> 27)), 10);
        Block material = style < 6 ? Blocks.DIRT_PATH : Blocks.COARSE_DIRT;
        if (style >= 8 && chunk.getBlockState(ground.below()).isFaceSturdy(level, ground.below(), Direction.UP)) {
            material = Blocks.GRAVEL;
        }
        level.setBlock(ground, material.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    private static boolean roadSoil(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.GRAVEL) || state.is(Blocks.DIRT_PATH);
    }

    private static boolean shortPlant(BlockState state) {
        return state.is(Blocks.GRASS) || state.is(Blocks.FERN) || state.is(Blocks.DEAD_BUSH) || state.is(Blocks.SNOW);
    }

    static void placeSign(ServerLevel level, BlockPos column, int bearing) {
        for (int shoulder : new int[]{(bearing + 2) % 8, (bearing + 6) % 8}) {
            for (int distance = 1; distance <= 2; distance++) {
                BlockPos side = column.offset(DX[shoulder] * distance, 0, DZ[shoulder] * distance);
                LevelChunk chunk = level.getChunkSource().getChunkNow(side.getX() >> 4, side.getZ() >> 4);
                if (chunk == null) continue;
                BlockPos ground = surface(chunk, side);
                BlockPos pos = ground.above();
                BlockState cover = chunk.getBlockState(pos);
                BlockState soil = chunk.getBlockState(ground);
                if ((!cover.isAir() && !shortPlant(cover)) || !signSupport(soil)
                        || !soil.isFaceSturdy(level, ground, Direction.UP)) continue;
                BlockState sign = Blocks.OAK_SIGN.defaultBlockState()
                        .setValue(StandingSignBlock.ROTATION, Math.floorMod(bearing * 2, 16));
                if (!level.setBlock(pos, sign, Block.UPDATE_CLIENTS)
                        || !(level.getBlockEntity(pos) instanceof SignBlockEntity entity)) continue;
                entity.updateText(text -> text
                        .setMessage(0, Component.literal("Потрібно"))
                        .setMessage(1, Component.literal("перейти гірський"))
                        .setMessage(2, Component.literal("перевал"))
                        .setMessage(3, Component.literal("Напрямок: " + COMPASS[bearing])), true);
                entity.setWaxed(true);
                entity.setChanged();
                level.sendBlockUpdated(pos, sign, sign, Block.UPDATE_CLIENTS);
                return;
            }
        }
    }

    private static boolean signSupport(BlockState state) {
        return roadSoil(state) || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE) || state.is(Blocks.TERRACOTTA);
    }
}
