package com.diplomacy.villagediplomacy.world.roads;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class VillageRoadData extends SavedData {
    static final String STORAGE_KEY = "village_diplomacy_roads";
    private static final long MAX_DISTANCE_SQUARED = 1_000L * 1_000;

    record Link(BlockPos first, BlockPos second) {
        static Link between(BlockPos a, BlockPos b) {
            return Long.compareUnsigned(a.asLong(), b.asLong()) <= 0 ? new Link(a, b) : new Link(b, a);
        }
    }

    /** Negative bearing is a road block. Nonnegative bearing is a pass sign. */
    record Plot(BlockPos column, int bearing) {}

    private final Set<BlockPos> villages = new LinkedHashSet<>();
    private final Set<Link> awaitingPlan = new LinkedHashSet<>();
    private final Map<Long, ArrayDeque<Plot>> pending = new LinkedHashMap<>();

    static VillageRoadData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(VillageRoadData::load, VillageRoadData::new, STORAGE_KEY);
    }

    void discover(BlockPos location) {
        BlockPos village = new BlockPos(location.getX(), 0, location.getZ());
        if (!villages.add(village)) return;
        for (BlockPos other : villages) {
            if (!other.equals(village) && withinRange(village, other)) {
                awaitingPlan.add(Link.between(village, other));
            }
        }
        setDirty();
    }

    static boolean withinRange(BlockPos a, BlockPos b) {
        long dx = (long) a.getX() - b.getX();
        long dz = (long) a.getZ() - b.getZ();
        return dx * dx + dz * dz <= MAX_DISTANCE_SQUARED;
    }

    Link nextLink() {
        return awaitingPlan.isEmpty() ? null : awaitingPlan.iterator().next();
    }

    Set<Long> finishPlanning(Link link, List<Plot> plots) {
        awaitingPlan.remove(link);
        Set<Long> chunks = new LinkedHashSet<>();
        for (Plot plot : plots) {
            long chunk = new ChunkPos(plot.column()).toLong();
            pending.computeIfAbsent(chunk, ignored -> new ArrayDeque<>()).addLast(plot);
            chunks.add(chunk);
        }
        setDirty();
        return chunks;
    }

    boolean hasPlots(long chunk) {
        return pending.containsKey(chunk);
    }

    Plot pollPlot(long chunk) {
        var queue = pending.get(chunk);
        if (queue == null) return null;
        Plot plot = queue.removeFirst();
        if (queue.isEmpty()) pending.remove(chunk);
        setDirty();
        return plot;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLongArray("Villages", villages.stream().mapToLong(BlockPos::asLong).toArray());
        ListTag pairs = new ListTag();
        for (Link link : awaitingPlan) {
            CompoundTag pair = new CompoundTag();
            pair.putLong("A", link.first().asLong());
            pair.putLong("B", link.second().asLong());
            pairs.add(pair);
        }
        tag.put("AwaitingPlan", pairs);
        ListTag plots = new ListTag();
        for (var queue : pending.values()) {
            for (Plot plot : queue) {
                CompoundTag column = new CompoundTag();
                column.putLong("Pos", plot.column().asLong());
                column.putByte("Bearing", (byte) plot.bearing());
                plots.add(column);
            }
        }
        tag.put("Plots", plots);
        return tag;
    }

    static VillageRoadData load(CompoundTag tag) {
        VillageRoadData data = new VillageRoadData();
        for (long position : tag.getLongArray("Villages")) data.villages.add(BlockPos.of(position));
        for (Tag entry : tag.getList("AwaitingPlan", Tag.TAG_COMPOUND)) {
            CompoundTag pair = (CompoundTag) entry;
            data.awaitingPlan.add(Link.between(BlockPos.of(pair.getLong("A")), BlockPos.of(pair.getLong("B"))));
        }
        for (Tag entry : tag.getList("Plots", Tag.TAG_COMPOUND)) {
            CompoundTag column = (CompoundTag) entry;
            Plot plot = new Plot(BlockPos.of(column.getLong("Pos")), column.getByte("Bearing"));
            data.pending.computeIfAbsent(new ChunkPos(plot.column()).toLong(), ignored -> new ArrayDeque<>()).addLast(plot);
        }
        return data;
    }
}
