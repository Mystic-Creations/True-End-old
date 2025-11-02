package net.mysticcreations.true_end.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.mysticcreations.true_end.TrueEnd;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Variables {
    public static final Capability<Variables.PlayerVariables> PLAYER_VARS_CAP = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<Variables.MapVariables> MAP_VARIABLES_CAP = CapabilityManager.get(new CapabilityToken<>() {});

    public static double randomEventChance;
    public static double entitySpawnChance;
    public static boolean doRandomEvents;
    public static boolean doDaytimeChange;
    public static boolean doFlashingLights;
    public static boolean showFog;
    public static boolean showCredits;
    public static int btdConversationDelay;
    public static boolean clearDreamItems;

    public static boolean showFogClient;
    public static boolean doChatReplies;

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent evt) {
        TrueEnd.PACKET_HANDLER.registerMessage(
            0,
            MapVariablesSyncMessage.class,
            MapVariablesSyncMessage::encode,
            MapVariablesSyncMessage::decode,
            MapVariablesSyncMessage::handle
        );
        TrueEnd.PACKET_HANDLER.registerMessage(
            1,
            PlayerVariablesSyncMessage.class,
            PlayerVariablesSyncMessage::encode,
            PlayerVariablesSyncMessage::decode,
            PlayerVariablesSyncMessage::handle
        );
    }

    @SubscribeEvent
    public static void onRegisterCaps(RegisterCapabilitiesEvent evt) {
        evt.register(PlayerVariables.class);
        evt.register(MapVariables.class);
    }

    public static class MapVariables extends SavedData {
        public static final String DATA_NAME = "true_end_mapvars";

        private double btdSpawnX = 0.0;
        private double btdSpawnY = 0.0;
        private double btdSpawnZ = 0.0;
        private boolean unknownInWorld = false;
        private int btdConversationDelay = 0;
        private double randomEventChance = 0.0;
        private boolean doRandomEvents = false;
        private boolean doFlashingLights = false;
        private boolean doDaytimeChange = false;
        private boolean clearDreamItems = false;
        private boolean doChatReplies = true;

        public boolean isUnknownInWorld() { return unknownInWorld; }
        public double getBtdSpawnX() { return btdSpawnX; }
        public double getBtdSpawnY() { return btdSpawnY; }
        public double getBtdSpawnZ() { return btdSpawnZ; }

        public void setUnknownInWorld(boolean v) { unknownInWorld = v; setDirty(); }
        public void setBtdSpawn(double x, double y, double z) { btdSpawnX = x; btdSpawnY = y; btdSpawnZ = z; setDirty(); }

        public static MapVariables load(CompoundTag nbt) {
            MapVariables m = new MapVariables();
            m.btdSpawnX = nbt.getDouble("btdSpawnX");
            m.btdSpawnY = nbt.getDouble("btdSpawnY");
            m.btdSpawnZ = nbt.getDouble("btdSpawnZ");
            m.unknownInWorld = nbt.getBoolean("unknownInWorld");
            m.btdConversationDelay = nbt.getInt("btdConversationDelay");
            m.randomEventChance = nbt.getDouble("randomEventChance");
            m.doRandomEvents = nbt.getBoolean("doRandomEvents");
            m.doFlashingLights = nbt.getBoolean("doFlashingLights");
            m.doDaytimeChange = nbt.getBoolean("doDaytimeChange");
            m.clearDreamItems = nbt.getBoolean("clearDreamItems");
            m.doChatReplies = nbt.getBoolean("doChatReplies");
            return m;
        }

        @Override
        public CompoundTag save(CompoundTag nbt) {
            nbt.putDouble("btdSpawnX", btdSpawnX);
            nbt.putDouble("btdSpawnY", btdSpawnY);
            nbt.putDouble("btdSpawnZ", btdSpawnZ);
            nbt.putBoolean("unknownInWorld", unknownInWorld);
            nbt.putInt("btdConversationDelay", btdConversationDelay);
            nbt.putDouble("randomEventChance", randomEventChance);
            nbt.putBoolean("doRandomEvents", doRandomEvents);
            nbt.putBoolean("doFlashingLights", doFlashingLights);
            nbt.putBoolean("doDaytimeChange", doDaytimeChange);
            nbt.putBoolean("clearDreamItems", clearDreamItems);
            nbt.putBoolean("doChatReplies", doChatReplies);
            return nbt;
        }

        public static MapVariables get(LevelAccessor world) {
            if (!(world instanceof ServerLevel lvl)) return new MapVariables();
            ServerLevel overworld = lvl.getServer().getLevel(Level.OVERWORLD);
            return overworld.getDataStorage().computeIfAbsent(MapVariables::load, MapVariables::new, DATA_NAME);
        }

        public void syncAll(LevelAccessor world) {
            if (world instanceof ServerLevel lvl) {
                TrueEnd.PACKET_HANDLER.send(
                        PacketDistributor.ALL.noArg(),
                        new MapVariablesSyncMessage(this)
                );
            }
        }
    }

    public static class MapVariablesSyncMessage {
        private final CompoundTag data;

        public MapVariablesSyncMessage(MapVariables vars) {
            this.data = vars.save(new CompoundTag());
        }

        public static void encode(MapVariablesSyncMessage msg, FriendlyByteBuf buf) {
            buf.writeNbt(msg.data);
        }
        public static MapVariablesSyncMessage decode(FriendlyByteBuf buf) {
            return new MapVariablesSyncMessage(MapVariables.load(buf.readNbt()));
        }
        public static void handle(MapVariablesSyncMessage msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                MapVariables client = MapVariables.get(Minecraft.getInstance().level);
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class PlayerVariables {
        private boolean beenBeyond = false;
        private boolean leftBtd = false;
        private int seepingRealityTime = 0;

        public boolean hasBeenBeyond() { return beenBeyond; }
        public void setBeenBeyond(boolean v) { beenBeyond = v; }
        public boolean hasLeftBtd() { return leftBtd; }
        public void setLeftBtd(boolean v) { leftBtd = v; }

        public int getSeepingRealityTime() { return seepingRealityTime; }
        public void setSeepingRealityTime(int v) { seepingRealityTime = v; }

        public void sync(ServerPlayer player) {
            TrueEnd.PACKET_HANDLER.send(
                PacketDistributor.PLAYER.with(() -> player),
                new PlayerVariablesSyncMessage(this)
            );
        }

        public CompoundTag writeNBT() {
            CompoundTag n = new CompoundTag();
            n.putBoolean("beenBeyond", beenBeyond);
            n.putBoolean("leftBtd", leftBtd);
            n.putInt("seepingRealityTime", seepingRealityTime);
            return n;
        }

        public void readNBT(CompoundTag n) {
            beenBeyond = n.getBoolean("beenBeyond");
            leftBtd = n.getBoolean("leftBtd");
            seepingRealityTime = n.getInt("seepingRealityTime");
        }
    }

    public static class PlayerVariablesSyncMessage {
        private final CompoundTag data;

        public PlayerVariablesSyncMessage(FriendlyByteBuf buf) {
            this.data = buf.readNbt();
        }

        public PlayerVariablesSyncMessage(PlayerVariables vars) {
            this.data = vars.writeNBT();
        }

        public static void encode(PlayerVariablesSyncMessage msg, FriendlyByteBuf buf) {
            buf.writeNbt(msg.data);
        }

        public static PlayerVariablesSyncMessage decode(FriendlyByteBuf buf) {
            return new PlayerVariablesSyncMessage(buf);
        }

        public static void handle(PlayerVariablesSyncMessage msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Minecraft.getInstance().player.getCapability(PLAYER_VARS_CAP).ifPresent(v ->
                    v.readNBT(msg.data)
                );
            });
            ctx.get().setPacketHandled(true);
        }
    }

    @Mod.EventBusSubscriber
    public static class PlayerVariablesProvider implements ICapabilitySerializable<CompoundTag> {
        private final PlayerVariables vars = new PlayerVariables();
        private final LazyOptional<PlayerVariables> opt = LazyOptional.of(() -> vars);

        @SubscribeEvent
        public static void attachCaps(AttachCapabilitiesEvent<Entity> evt) {
            if (evt.getObject() instanceof Player && evt.getObject() instanceof ServerPlayer) {
                evt.addCapability(
                        ResourceLocation.parse("true_end:player_variables"),
                        new PlayerVariablesProvider()
                );
            }
        }

        @SubscribeEvent
        public static void cloneOnDeath(PlayerEvent.Clone evt) {
            if (!evt.isWasDeath()) return;
            evt.getOriginal().reviveCaps();
            evt.getOriginal().getCapability(PLAYER_VARS_CAP).ifPresent(oldV ->
                evt.getEntity().getCapability(PLAYER_VARS_CAP).ifPresent(newV -> {
                            newV.setBeenBeyond(oldV.hasBeenBeyond());
                            newV.setSeepingRealityTime(oldV.getSeepingRealityTime());
                        }
                )
            );
        }

        @SubscribeEvent
        public static void syncOnLogin(PlayerEvent.PlayerLoggedInEvent evt) {
            evt.getEntity().getCapability(PLAYER_VARS_CAP).ifPresent(v -> v.sync((ServerPlayer)evt.getEntity()));
        }
        @SubscribeEvent
        public static void syncOnRespawn(PlayerEvent.PlayerRespawnEvent evt) {
            evt.getEntity().getCapability(PLAYER_VARS_CAP).ifPresent(v -> v.sync((ServerPlayer)evt.getEntity()));
        }
        @SubscribeEvent
        public static void syncOnDimChange(PlayerEvent.PlayerChangedDimensionEvent evt) {
            evt.getEntity().getCapability(PLAYER_VARS_CAP).ifPresent(v -> v.sync((ServerPlayer)evt.getEntity()));
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, net.minecraft.core.Direction side) {
            return cap == PLAYER_VARS_CAP ? opt.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() { return vars.writeNBT(); }
        @Override
        public void deserializeNBT(CompoundTag nbt) { vars.readNBT(nbt); }
    }
}