package xepel.sic_admin;

import com.fs.starfarer.api.BaseModPlugin;

public class SiCAdministrationPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();

        // Test that the .jar is loaded and working, using the most obnoxious way possible.
        //throw new RuntimeException("Template mod loaded and working!\nRemove this crash in SiCAdministrationPlugin.");
    }

    // You can add more methods from ModPlugin here. Press Control-O in IntelliJ to see options.
}
