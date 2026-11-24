package com.diplomacy.villagediplomacy.world.roads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Function;

/** Bounded terrain search over a coarse grid, with a walkable block-width trace. */
final class RoadPlanner {
    private static final int STEP = 8;
    private static final int DETOUR = 160;
    private static final int MAX_EXPANSIONS = 8_000;

    record Point(int x, int z) {
        double distance(Point other) {
            return Math.hypot(x - (double) other.x, z - (double) other.z);
        }
    }

    record Terrain(int height, boolean river, boolean lake, boolean forest, boolean mountain) {}
    record Segment(Point from, Point to, boolean pass) {}
    private record Grid(int x, int z) {}
    private record Visit(Grid grid, double cost, double priority) implements Comparable<Visit> {
        @Override public int compareTo(Visit other) {
            return Double.compare(priority, other.priority);
        }
    }

    private final Point start;
    private final Point end;
    private final Grid first;
    private final Grid last;
    private final Function<Point, Terrain> survey;
    private final Map<Grid, Terrain> terrain = new HashMap<>();
    private final Map<Grid, Double> costs = new HashMap<>();
    private final Map<Grid, Grid> previous = new HashMap<>();
    private final PriorityQueue<Visit> open = new PriorityQueue<>();
    private int expanded;
    private List<Segment> result;

    RoadPlanner(Point start, Point end, Function<Point, Terrain> survey) {
        this.start = start;
        this.end = end;
        this.survey = survey;
        first = grid(start);
        last = grid(end);
        costs.put(first, 0.0);
        open.add(new Visit(first, 0, start.distance(end)));
    }

    /** Limit terrain queries on each server tick. */
    boolean advance(int budget) {
        while (budget-- > 0 && !open.isEmpty() && expanded < MAX_EXPANSIONS) {
            Visit visit = open.remove();
            if (visit.cost() != costs.get(visit.grid())) continue;
            if (visit.grid().equals(last)) {
                result = trace();
                return true;
            }
            expanded++;
            Terrain origin = sample(visit.grid());
            for (int dz = -1; dz <= 1; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dz == 0) continue;
                    Grid next = new Grid(visit.grid().x() + dx, visit.grid().z() + dz);
                    Point at = point(next);
                    if (!withinDetour(at)) continue;
                    Terrain target = sample(next);
                    double cost = visit.cost() + point(visit.grid()).distance(at)
                            + terrainPenalty(origin, target);
                    if (cost >= costs.getOrDefault(next, Double.POSITIVE_INFINITY)) continue;
                    costs.put(next, cost);
                    previous.put(next, visit.grid());
                    open.add(new Visit(next, cost, cost + at.distance(end) * 1.6));
                }
            }
        }
        return open.isEmpty() || expanded >= MAX_EXPANSIONS;
    }

    List<Segment> result() {
        return result == null ? List.of() : result;
    }

    private static double terrainPenalty(Terrain from, Terrain to) {
        return (to.lake() ? 160 : to.river() ? 9 : 0)
                + (to.forest() ? 16 : 0)
                + Math.abs(to.height() - from.height()) * 1.5
                + (requiresPass(from, to) ? 35 : 0);
    }

    private static boolean requiresPass(Terrain from, Terrain to) {
        if (from.river() || from.lake() || to.river() || to.lake()) return false;
        int slope = Math.abs(to.height() - from.height());
        return slope >= 7 || ((from.mountain() || to.mountain())
                && (slope >= 4 || Math.max(from.height(), to.height()) >= 120));
    }

    private List<Segment> trace() {
        List<Segment> path = new ArrayList<>();
        for (Grid current = last; !current.equals(first); ) {
            Grid parent = previous.get(current);
            path.add(new Segment(point(parent), point(current), requiresPass(sample(parent), sample(current))));
            current = parent;
        }
        Collections.reverse(path);
        return path;
    }

    private Terrain sample(Grid cell) {
        return terrain.computeIfAbsent(cell, key -> survey.apply(point(key)));
    }

    private static Grid grid(Point point) {
        return new Grid(Math.floorDiv(point.x(), STEP), Math.floorDiv(point.z(), STEP));
    }

    private Point point(Grid cell) {
        if (cell.equals(first)) return start;
        if (cell.equals(last)) return end;
        return new Point(cell.x() * STEP + STEP / 2, cell.z() * STEP + STEP / 2);
    }

    private boolean withinDetour(Point point) {
        return point.x() >= Math.min(start.x(), end.x()) - DETOUR
                && point.x() <= Math.max(start.x(), end.x()) + DETOUR
                && point.z() >= Math.min(start.z(), end.z()) - DETOUR
                && point.z() <= Math.max(start.z(), end.z()) + DETOUR;
    }

    /** Rasterize with cardinal steps, so adjacent blocks share an edge. */
    static List<Point> raster(Point from, Point to) {
        List<Point> columns = new ArrayList<>();
        int x = from.x();
        int z = from.z();
        int dx = Math.abs(to.x() - x);
        int dz = Math.abs(to.z() - z);
        int sx = Integer.compare(to.x(), x);
        int sz = Integer.compare(to.z(), z);
        int error = dx - dz;
        columns.add(from);
        while (x != to.x() || z != to.z()) {
            int twice = error * 2;
            if (x != to.x() && twice >= -dz) {
                error -= dz;
                x += sx;
                columns.add(new Point(x, z));
            }
            if (z != to.z() && twice <= dx) {
                error += dx;
                z += sz;
                columns.add(new Point(x, z));
            }
        }
        return columns;
    }
}
