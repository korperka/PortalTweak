package hi.korperka.portaltweak;

import hi.korperka.portaltweak.listener.PortalIgniteListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class PortalTweak extends JavaPlugin {

    @Override
    public void onEnable() {
        new PortalIgniteListener(this).registerListener();
    }

    public static PortalTweak getInstance() {
        return PortalTweak.getPlugin(PortalTweak.class);
    }
}
