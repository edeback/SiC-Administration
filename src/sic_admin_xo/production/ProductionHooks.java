package sic_admin_xo.production;

import com.fs.starfarer.api.Global;
import sic_admin_xo.skills.administration.OptimizedShipbuilding;

/**
 * Registers a hook for every ship production system we know about. Each hook checks whether the
 * skill is active when it fires, so registration doesn't have to track the officer being assigned.
 */
public class ProductionHooks {

    public static final String AOTD_TOOLBOX_ID = "aotd_theory_of_toolbox";

    /**
     * Called from onGameLoad. The listeners are transient, so they never end up in the save and are
     * rebuilt from scratch on every load.
     */
    public static void register() {
        // Saves from before the hooks moved out of the skill plugin have it registered as a
        // (non-transient) listener of its own.
        Global.getSector().getListenerManager().removeListenerOfClass(OptimizedShipbuilding.class);

        Global.getSector().getListenerManager().removeListenerOfClass(VanillaProductionHook.class);
        Global.getSector().getListenerManager().addListener(new VanillaProductionHook(), true);

        // AoTDProductionHook implements an interface from another mod's jar, so it must not be
        // loaded at all unless that mod is present.
        if (Global.getSettings().getModManager().isModEnabled(AOTD_TOOLBOX_ID)) {
            try {
                AoTDProductionHook.register();
            } catch (Throwable t) {
                Global.getLogger(ProductionHooks.class).error("Failed to register the AoTD production hook", t);
            }
        }
    }
}
