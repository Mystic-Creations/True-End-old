package net.mysticcreations.true_end.procedures.alphaFeatures;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
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
    public static Map<Item, Float> FOOD_MAP = new HashMap<>();

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
        if (event.getHand() != InteractionHand.MAIN_HAND) return 0;
        Player player = event.getEntity();
        if (player == null) return 0;
        if (player.level().dimension() != BTD) return 0;

        ItemStack stack = event.getItemStack();
        Float heal = FOOD_MAP.get(stack.getItem());
        float newHealth = player.getHealth();

        int consumed;
        if (heal != null) {
            newHealth += heal;
            consumed = 1;
        } else if (stack.isEdible()) {
            //Cancel if edible but not any of the listed items
            consumed = 0;
        } else {
            //Not edible but right-clickable? Skip cancelling
            consumed = 2;
        }

        if (consumed == 1) {
            if (!healthCheck(player, event)) {
                stack.shrink(1);
                player.getInventory().setChanged();
                float maxHealth = player.getMaxHealth();
                player.setHealth(Math.min(newHealth, maxHealth));
                playEatSound(player.level(), player.getX(), player.getY(), player.getZ());
                event.setCanceled(true);
            } else {
                consumed = 0;
            }
        }

        if (consumed == 0) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            }
        }
        return consumed;
    }

    @SubscribeEvent
    public static int onRightClickItem(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return 0;
        Player player = event.getEntity();
        if (player == null) return 0;
        if (player.level().dimension() != BTD) return 0;

        ItemStack stack = event.getItemStack();
        Float heal = FOOD_MAP.get(stack.getItem());

        int consumed;
        if (heal != null) {
            consumed = 0;
        } else if (stack.isEdible()) {
            consumed = 0;
        } else {
            consumed = 2;
        }

        if (consumed == 0) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            }
        }
        return consumed;
    }

    private static void playEatSound(LevelAccessor world, double x, double y, double z) {
        double pitch = 0.8 + Math.random() * 0.4;

        if (world instanceof Level _level) {
            if (!_level.isClientSide()) {
                _level.playSound(null, BlockPos.containing(x, y, z),
                    Objects.requireNonNull(
                        ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse("entity.generic.eat"))),
                    SoundSource.NEUTRAL, 1.0f, (float) pitch);
            } else {
                _level.playLocalSound(x, y, z,
                    Objects.requireNonNull(
                        ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse("entity.generic.eat"))),
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
