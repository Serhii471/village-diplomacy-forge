package com.diplomacy.villagediplomacy.world.roads;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class VillageRoadPlannerTest {
    private static final RoadPlanner.Point WEST = new RoadPlanner.Point(0, 0);
    private static final RoadPlanner.Point EAST = new RoadPlanner.Point(96, 0);
    private static final RoadPlanner.Terrain LAND = new RoadPlanner.Terrain(64, false, false, false, false);

    @Test
    void takesAnEdgeConnectedDetourAroundLakeAndForest() {
        var trace = plan(point -> {
            if (point.x() >= 24 && point.x() <= 64 && Math.abs(point.z()) <= 12) {
                return new RoadPlanner.Terrain(64, false, point.x() < 48, point.x() >= 48, false);
            }
            return LAND;
        });
        assertTrue(trace.stream().anyMatch(segment -> Math.abs(segment.to().z()) > 12));
        assertTrue(trace.stream().noneMatch(segment -> segment.to().x() >= 24 && segment.to().x() <= 64
                && Math.abs(segment.to().z()) <= 12));
        for (var segment : trace) {
            var columns = RoadPlanner.raster(segment.from(), segment.to());
            for (int i = 1; i < columns.size(); i++) assertEquals(1.0, columns.get(i - 1).distance(columns.get(i)));
        }
    }

    @Test
    void aRiverDoesNotTurnIntoAMountainPass() {
        var trace = plan(point -> point.x() >= 40 && point.x() <= 48
                ? new RoadPlanner.Terrain(62, true, false, false, false) : LAND);
        assertTrue(trace.stream().noneMatch(RoadPlanner.Segment::pass));
        assertTrue(trace.stream().anyMatch(segment -> segment.to().x() > 48));
    }

    @Test
    void marksBothEndsOfUnavoidableMountainPass() {
        var trace = plan(point -> point.x() >= 32 && point.x() <= 64
                ? new RoadPlanner.Terrain(132, false, false, false, true) : LAND);
        var plots = VillageRoads.plots(trace);
        var signs = plots.stream().filter(plot -> plot.bearing() >= 0).toList();
        assertEquals(2, signs.size());
        assertEquals(List.of(2, 6), signs.stream().map(VillageRoadData.Plot::bearing).toList());
        assertTrue(plots.stream().noneMatch(plot -> plot.bearing() < 0 && plot.column().getX() == 48));
    }

    @Test
    void respectsTheHorizontalRadiusAndKeepsUnbuiltColumnsAcrossReloads() {
        var inside = new BlockPos(600, 200, 800);
        assertTrue(VillageRoadData.withinRange(BlockPos.ZERO, inside));
        assertFalse(VillageRoadData.withinRange(BlockPos.ZERO, inside.east()));
        var data = new VillageRoadData();
        data.discover(BlockPos.ZERO);
        assertNull(data.nextLink());
        data.discover(inside);
        data = VillageRoadData.load(data.save(new CompoundTag()));
        var link = data.nextLink();
        assertNotNull(link);
        var plot = new VillageRoadData.Plot(new BlockPos(16, 0, 17), 2);
        data.finishPlanning(link, List.of(plot));
        data = VillageRoadData.load(data.save(new CompoundTag()));
        data.discover(inside.above());
        assertNull(data.nextLink());
        long chunk = new ChunkPos(plot.column()).toLong();
        assertEquals(plot, data.pollPlot(chunk));
        assertFalse(data.hasPlots(chunk));
    }

    private static List<RoadPlanner.Segment> plan(Function<RoadPlanner.Point, RoadPlanner.Terrain> survey) {
        var planner = new RoadPlanner(WEST, EAST, survey);
        for (int ticks = 0; ticks < 4_001; ticks++) {
            if (planner.advance(2)) {
                var path = planner.result();
                assertFalse(path.isEmpty());
                return path;
            }
        }
        throw new AssertionError("Terrain search never finished");
    }
}
