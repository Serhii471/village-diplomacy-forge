package com.diplomacy.villagediplomacy.menu;

import com.diplomacy.villagediplomacy.diplomacy.Settlement;
import com.diplomacy.villagediplomacy.entity.VillageGuardEntity;
import com.diplomacy.villagediplomacy.garrison.GarrisonSavedData;
import com.diplomacy.villagediplomacy.garrison.GarrisonService;
import com.diplomacy.villagediplomacy.garrison.ResidentData;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record GarrisonSnapshot(String homeName, boolean canManage, int civilians, int trainees, int soldiers,
                               int limit, boolean room, List<Entry> entries, String message) {
    private static final int MAX_ENTRIES = 256;
    // Reserve space for the menu header, table position and Forge framing.
    static final int MAX_ROSTER_BYTES = DiplomacyTableSnapshot.MAX_NETWORK_BYTES - 2_000;

    public record Entry(UUID identity, Component name, GarrisonSavedData.Role role, boolean canAct, long remaining) { }
    public static GarrisonSnapshot create(ServerLevel level, ServerPlayer player, Settlement home, String message) {
        GarrisonService.discover(level);
        GarrisonService.Population population = GarrisonService.population(level, home);
        List<Entry> entries = new ArrayList<>();
        for (var person : GarrisonSavedData.get(level).people()) {
            if (!person.home().equals(home.getId())) continue;
            var entity = level.getEntity(person.entity());
            Component name = Component.translatable("name.villagediplomacy.resident." + Math.floorMod(person.identity().hashCode(), 12));
            boolean canAct = false; long remaining = 0;
            if (entity instanceof Villager villager) {
                name = ResidentData.name(villager);
                canAct = person.role() == GarrisonSavedData.Role.TRAINING || GarrisonService.eligible(villager);
                remaining = ResidentData.isTraining(villager) ? ResidentData.remaining(villager) : 0;
            } else if (entity instanceof VillageGuardEntity guard) {
                name = guard.getName();
                canAct = person.recruited() && guard.isAlive() && guard.getTarget() == null;
            }
            entries.add(new Entry(person.identity(), name, person.role(), canAct, remaining));
        }
        entries.sort(Comparator.comparingInt((Entry e) -> e.role() == GarrisonSavedData.Role.TRAINING ? 0
                : e.role() == GarrisonSavedData.Role.SOLDIER ? 1 : 2).thenComparing(e -> e.identity().toString()));
        String homeName = home.getName();
        if (homeName.length() > 256) homeName = homeName.substring(0, 256);
        return new GarrisonSnapshot(homeName, home.isPlayerLeader(player.getUUID()), population.civilians(),
                population.trainees(), population.soldiers(), population.limit(), population.canRecruit(),
                fitRoster(entries), message);
    }

    static List<Entry> fitRoster(List<Entry> sortedEntries) {
        List<Entry> result = new ArrayList<>();
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            for (Entry source : sortedEntries) {
                if (result.size() == MAX_ENTRIES) break;
                Entry entry = source;
                if (Component.Serializer.toJson(source.name()).length() > 512) {
                    Component name = Component.translatable("name.villagediplomacy.resident."
                            + Math.floorMod(source.identity().hashCode(), 12));
                    entry = new Entry(source.identity(), name, source.role(), source.canAct(), source.remaining());
                }
                writeEntry(buffer, entry);
                if (buffer.readableBytes() > MAX_ROSTER_BYTES) break;
                result.add(entry);
            }
            return List.copyOf(result);
        } finally {
            buffer.release();
        }
    }
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(homeName, 256); buffer.writeBoolean(canManage);
        buffer.writeVarInt(civilians); buffer.writeVarInt(trainees); buffer.writeVarInt(soldiers); buffer.writeVarInt(limit);
        buffer.writeBoolean(room); buffer.writeUtf(message, 64); buffer.writeVarInt(entries.size());
        entries.forEach(entry -> writeEntry(buffer, entry));
    }

    private static void writeEntry(FriendlyByteBuf buffer, Entry entry) {
        buffer.writeUUID(entry.identity());
        buffer.writeComponent(entry.name());
        buffer.writeEnum(entry.role());
        buffer.writeBoolean(entry.canAct());
        buffer.writeVarLong(entry.remaining());
    }
    public static GarrisonSnapshot read(FriendlyByteBuf buffer) {
        String name = buffer.readUtf(256); boolean manage = buffer.readBoolean();
        int civilians = buffer.readVarInt(), trainees = buffer.readVarInt(), soldiers = buffer.readVarInt(), limit = buffer.readVarInt();
        boolean room = buffer.readBoolean(); String message = buffer.readUtf(64); int count = buffer.readVarInt();
        if (count < 0 || count > MAX_ENTRIES) throw new IllegalArgumentException("Invalid roster size");
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) entries.add(new Entry(buffer.readUUID(), buffer.readComponent(),
                buffer.readEnum(GarrisonSavedData.Role.class), buffer.readBoolean(), buffer.readVarLong()));
        return new GarrisonSnapshot(name, manage, civilians, trainees, soldiers, limit, room, List.copyOf(entries), message);
    }
}
