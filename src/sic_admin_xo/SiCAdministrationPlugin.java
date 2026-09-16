package sic_admin_xo;

import com.fs.starfarer.api.BaseModPlugin;
import sic_admin_xo.production.ProductionHooks;

public class SiCAdministrationPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();

        // Test that the .jar is loaded and working, using the most obnoxious way possible.
        //throw new RuntimeException("Template mod loaded and working!\nRemove this crash in SiCAdministrationPlugin.");
    }

    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);

        // Cached colony data belongs to the sector that produced it, and its timestamps are only
        // meaningful against that sector's clock.
        ColonyProximity.reset();

        ProductionHooks.register();
    }

    // You can add more methods from ModPlugin here. Press Control-O in IntelliJ to see options.
}
