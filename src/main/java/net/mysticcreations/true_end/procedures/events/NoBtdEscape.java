package net.mysticcreations.true_end.procedures.events;

import net.mysticcreations.true_end.network.Variables;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static net.mysticcreations.true_end.init.Dimensions.BTD;

@Mod.EventBusSubscriber
public class NoBtdEscape {
    private static final Map<UUID, ResourceKey<Level>> diedIn = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) return;
        diedIn.put(player.getUUID(), player.level().dimension());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Entity entity = event.getEntity();
        LevelAccessor world = entity.level();
        if (!(entity instanceof ServerPlayer player)) return;

        boolean leftBtd = player.getAdvancements()
            .getOrStartProgress(Objects.requireNonNull(
                player.server.getAdvancements().getAdvancement(ResourceLocation.parse("true_end:go_back")))
            ).isDone();
        if (leftBtd) return;

        UUID uuid = player.getUUID();
        ResourceKey<Level> dim = diedIn.remove(uuid);
        if (dim == null || dim != BTD) return;

        BlockPos respawnPos = player.getRespawnPosition();
        ResourceKey<Level> respawnDim = player.getRespawnDimension();

        if (respawnPos != null) {
            ServerLevel targetLevel = player.server.getLevel(respawnDim);
            if (targetLevel != null) {
                Optional<Vec3> maybeSpot = Player.findRespawnPositionAndUseSpawnBlock(targetLevel, respawnPos, 0, player.isRespawnForced(), false);

                if (maybeSpot.isPresent()) {
                    Vec3 spot = maybeSpot.get();
                    player.teleportTo(targetLevel, spot.x, spot.y+0.05, spot.z, 180, 0);
                    return;
                }

                double fallbackX = respawnPos.getX()+0.5;
                double fallbackY = respawnPos.getY();
                double fallbackZ = respawnPos.getZ()+0.5;
                player.teleportTo(targetLevel, fallbackX, fallbackY+0.05, fallbackZ, 180,0);
                return;
            }
        }

        double bx = Variables.MapVariables.get(world).getBtdSpawnX();
        double by = Variables.MapVariables.get(world).getBtdSpawnY();
        double bz = Variables.MapVariables.get(world).getBtdSpawnZ();
        ServerLevel btd = player.server.getLevel(BTD);
        if (btd == null) return;

        player.teleportTo(btd, bx, by+0.05, bz, 0, 0);
    }
}
