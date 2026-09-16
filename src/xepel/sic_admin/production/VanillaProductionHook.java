package xepel.sic_admin.production;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.intel.misc.HackProductionReport;
import com.fs.starfarer.api.impl.campaign.intel.misc.ProductionReportIntel;

import java.util.List;

/**
 * Vanilla custom production. CoreScript.doCustomProduction() runs from its own month-end listener
 * and drops the finished ships into the gathering point's storage, keeping the same FleetMemberAPI
 * instances in the ProductionReportIntel it posts. There's no "ship produced" callback, so the
 * report is the only handle on them - we run after CoreScript and stamp what it just delivered.
 */
public class VanillaProductionHook implements EconomyTickListener {

    public static final String CUSTOM_PRODUCTION_CARGO = "Heavy Industry - Custom Production";

    @Override
    public void reportEconomyTick(int iterIndex) { }

    @Override
    public void reportEconomyMonthEnd() {
        if (!ProducedShipStamper.isActive()) return;

        List<IntelInfoPlugin> reports = Global.getSector().getIntelManager().getIntel(ProductionReportIntel.class);
        for (IntelInfoPlugin plugin : reports) {
            ProductionReportIntel report = (ProductionReportIntel) plugin;

            ProductionReportIntel.ProductionData data = HackProductionReport.getProductionData(report);
            if (data == null) continue;

            // Not getCargo(), which would add an empty entry to reports that never had one.
            CargoAPI cargo = data.data.get(CUSTOM_PRODUCTION_CARGO);
            if (cargo == null || cargo.getMothballedShips() == null) continue;

            for (FleetMemberAPI member : cargo.getMothballedShips().getMembersListCopy()) {
                ProducedShipStamper.stamp(member);
            }
        }
    }
}
