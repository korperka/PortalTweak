package hi.korperka.portaltweak.listener;

import hi.korperka.portaltweak.PortalTweak;
import hi.korperka.portaltweak.stuff.Krasivsk;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

public class PortalIgniteListener implements Listener {
    private final PortalTweak plugin;
    private final NamespacedKey triedActivate;

    public PortalIgniteListener(PortalTweak plugin) {
        this.plugin = plugin;
        this.triedActivate = new NamespacedKey(plugin, "tried_activate_portal");
    }

    /**
     * <img src = https://i.imgur.com/1s80Onh.jpeg />
     * <br>
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();

        if(block == null || item == null) {
            return;
        }

        if(block.getType() != Material.OBSIDIAN || item.getType() != Material.CLOCK) {
            return;
        }

        Player player = event.getPlayer();
        World world = block.getWorld();
        Location location = block.getLocation();

        try {
            location.add(event.getBlockFace().getDirection());

            Method getHandle = world.getClass().getMethod("getHandle");
            Object nmsWorld = getHandle.invoke(world);
            Class<?> blockPositionClass = Class.forName("net.minecraft.core.BlockPos");
            Object blockPosition = blockPositionClass
                    .getConstructor(int.class, int.class, int.class)
                    .newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            Method getBlockState = nmsWorld.getClass().getMethod("getBlockState", blockPositionClass);
            Object currentBlockState = getBlockState.invoke(nmsWorld, blockPosition);

            Class<?> blocksClass = Class.forName("net.minecraft.world.level.block.Blocks");

            Field placeholderBlockField = blocksClass.getField("TURTLE_EGG");
            Object placeholderBlock = placeholderBlockField.get(null);
            Method defaultBlockState = placeholderBlock.getClass().getMethod("defaultBlockState");
            Object placeholderBlockState = defaultBlockState.invoke(placeholderBlock);

            Class<?> baseFireBlockClass = Class.forName("net.minecraft.world.level.block.BaseFireBlock");
            Method isPortalMethod = baseFireBlockClass.getDeclaredMethod(
                    "isPortal",
                    Class.forName("net.minecraft.world.level.Level"),
                    blockPositionClass,
                    Class.forName("net.minecraft.core.Direction")
            );
            isPortalMethod.setAccessible(true);

            Object direction = Enum.valueOf(
                    (Class<Enum>) Class.forName("net.minecraft.core.Direction"),
                    event.getBlockFace().toString()
            );

            boolean isPortal = (boolean) isPortalMethod.invoke(null, nmsWorld, blockPosition, direction);

            if(!isPortal) {
                return;
            }

            Field fireBlockField = blocksClass.getField("FIRE");
            Object fireBlock = fireBlockField.get(null);

            Method onPlaceMethod = baseFireBlockClass.getDeclaredMethod(
                    "onPlace",
                    Class.forName("net.minecraft.world.level.block.state.BlockState"),
                    Class.forName("net.minecraft.world.level.Level"),
                    blockPositionClass,
                    Class.forName("net.minecraft.world.level.block.state.BlockState"),
                    boolean.class,
                    Class.forName("net.minecraft.world.item.context.UseOnContext")
            );

            onPlaceMethod.setAccessible(true);

            onPlaceMethod.invoke(fireBlock,
                    currentBlockState,
                    nmsWorld,
                    blockPosition,
                    placeholderBlockState,
                    false,
                    null
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        Krasivsk.createPathAndSphereEffect(player.getEyeLocation(), location);
        player.getWorld().spawnParticle(Particle.BLOCK, player.getEyeLocation(), 20, Material.GOLD_BLOCK.createBlockData());
        item.setAmount(item.getAmount() - 1);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 1, 0.4f);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
    }

    @EventHandler
    public void onPortalActivate(PortalCreateEvent event) {
        for(StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (Objects.equals(element.getClassName(), PortalIgniteListener.class.getName()) && !element.getMethodName().equals("onPortalActivate")) {
                return;
            }
        }

        if(event.getReason() != PortalCreateEvent.CreateReason.FIRE) {
            return;
        }

        if(event.getEntity() instanceof Player player) {
            Block block = player.getTargetBlockExact(5);
            Location location = block == null ? player.getLocation() : block.getLocation();
            player.getWorld().spawnParticle(Particle.PORTAL, location, 200);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.3f);

            if(!player.getPersistentDataContainer().has(triedActivate)) {
                player.getPersistentDataContainer().set(triedActivate, PersistentDataType.BOOLEAN, true);
                player.sendMessage(Component.text("Хм... Я же точно помню, что он активировался как то так...").decorate(TextDecoration.ITALIC).color(TextColor.color(Color.LIGHT_GRAY.getRGB())));
            }
        }

        event.setCancelled(true);
    }

    public void registerListener() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}
