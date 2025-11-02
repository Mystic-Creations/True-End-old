package net.mysticcreations.true_end.procedures.alphaFeatures;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

import static net.mysticcreations.true_end.init.Dimensions.BTD;

@Mod.EventBusSubscriber
public class AlphaFoodSystem {
    public static final Map<Item, Float> FOOD_MAP = new ConcurrentHashMap<>();
    static {
        FOOD_MAP.put(Items.PORKCHOP, 1.5F);
        FOOD_MAP.put(Items.COOKED_PORKCHOP, 4.0F);
        FOOD_MAP.put(Items.BEEF, 3.0F);
        FOOD_MAP.put(Items.COOKED_BEEF, 8.0F);
        FOOD_MAP.put(Items.MUTTON, 2.0F);
        FOOD_MAP.put(Items.COOKED_MUTTON, 6.0F);
        FOOD_MAP.put(Items.CHICKEN, 2.0F);
        FOOD_MAP.put(Items.COOKED_CHICKEN, 6.0F);
        FOOD_MAP.put(Items.BREAD, 2.5F);
        FOOD_MAP.put(Items.APPLE, 2.0F);
        FOOD_MAP.put(Items.GOLDEN_APPLE, 10.0F);
    }

    @SubscribeEvent
    public static int onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        return processInteraction(event);
    }
    @SubscribeEvent
    public static int onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        return processInteraction(event);
    }

    private static int processInteraction(PlayerInteractEvent event) {
        Player player = event.getEntity();
        if (player == null) return 0;
        if (player.level().dimension() != BTD) return 0;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return 0;

        Item item = stack.getItem();
        Float heal = FOOD_MAP.get(item);

        int consumed = 2;
        if (heal != null) {
            consumed = 1;
        } else if (stack.isEdible()) {
            consumed = 0;
        }

        if (consumed == 1) {
            float newHealth = player.getHealth() + heal;
            if (!healthCheck(player, event)) {
                stack.shrink(1);
                player.getInventory().setChanged();
                float maxHealth = player.getMaxHealth();
                player.setHealth(Math.min(newHealth, maxHealth));
                playEatSound(player.level(), player.getX(), player.getY(), player.getZ());
            } else {
                consumed = 0;
            }
            if (event.isCancelable()) event.setCanceled(true);
        } else if (consumed == 0) {
            if (event.isCancelable()) event.setCanceled(true);
        }

        return consumed;
    }

    private static void playEatSound(LevelAccessor world, double x, double y, double z) {
        double pitch = 0.8 + Math.random() * 0.4;

        if (world instanceof Level level) {
            if (!level.isClientSide()) {
                level.playSound(null, BlockPos.containing(x, y, z),
                    Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse("entity.generic.eat"))),
                    SoundSource.NEUTRAL, 1.0f, (float) pitch);
            } else {
                level.playLocalSound(x, y, z,
                    Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse("entity.generic.eat"))),
                    SoundSource.NEUTRAL, 1.0f, (float) pitch, false);
            }
        }
    }

    private static boolean healthCheck(Player player, PlayerInteractEvent event) {
        if (player.getHealth() >= player.getMaxHealth()) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            }
            return true;
        } else {
            return false;
        }
    }
}
