package hi.korperka.portaltweak.stuff;

import hi.korperka.portaltweak.PortalTweak;
import org.bukkit.*;
import org.bukkit.util.Vector;

public class Krasivsk {
    public static void createPathAndSphereEffect(Location startLocation, Location endLocation) {
        if (startLocation == null || endLocation == null || startLocation.getWorld() == null) {
            return;
        }

        createSphereEffect(startLocation, 2.0, 30);

        createPathEffect(startLocation, endLocation, 1.0, 20);
    }

    private static void createSphereEffect(Location location, double radius, int count) {
        World world = location.getWorld();

        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            double phi = Math.random() * Math.PI;
            double x = radius * Math.sin(phi) * Math.cos(angle);
            double y = radius * Math.sin(phi) * Math.sin(angle);
            double z = radius * Math.cos(phi);

            world.spawnParticle(Particle.END_ROD, location.clone().add(x, y, z), 1);
        }
    }

    private static void createPathEffect(Location startLocation, Location endLocation, double stepSize, int particleCount) {
        Vector direction = endLocation.clone().subtract(startLocation).toVector().normalize();
        double distance = startLocation.distance(endLocation);

        for (double t = 0; t < distance; t += stepSize) {
            Location particleLocation = startLocation.clone().add(direction.clone().multiply(t));
            startLocation.getWorld().spawnParticle(Particle.END_ROD, particleLocation, particleCount, 0.1, 0.1, 0.1, 0.05);
        }
    }
}
