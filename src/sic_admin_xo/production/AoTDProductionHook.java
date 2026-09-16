package sic_admin_xo.production;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.kaysaar.aotd.tot.produciton.listeners.AoTDProductionListenerAPI;

/**
 * Ashes of the Domain replaces custom production with its own continuous system, which never posts
 * a vanilla ProductionReportIntel. It does fire this callback from
 * AoTDProductionOrderData.addReward(), after variant.clear() and before the ship goes into storage,
 * so a permanent hullmod added here survives.
 *
 * Only touched when aotd_theory_of_toolbox is enabled - see ProductionHooks.
 */
public class AoTDProductionHook implements AoTDProductionListenerAPI {

    public static void register() {
        Global.getSector().getListenerManager().removeListenerOfClass(AoTDProductionHook.class);
        Global.getSector().getListenerManager().addListener(new AoTDProductionHook(), true);
    }

    @Override
    public void onShipProductionFinished(FleetMemberAPI member) {
        ProducedShipStamper.stamp(member);
    }
}
