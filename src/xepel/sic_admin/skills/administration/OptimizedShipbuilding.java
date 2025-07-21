package xepel.sic_admin.skills.administration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionProductionAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.misc.HackProductionReport;
import com.fs.starfarer.api.impl.campaign.intel.misc.ProductionReportIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

import java.util.*;

public class OptimizedShipbuilding extends SCBaseSkillPlugin implements EconomyTickListener {

    public static String MOD_ID = "sic_admin_optimized_shipbuilding";
    public static float CUSTOM_PRODUCTION_BONUS = 50f;
    public static String HULLMOD_ID = "sic_admin_optimized_hull";

    @Override
    public String getAffectsString() {
        return "governed colonies";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("+50%% maximum value of custom ship and weapon production per month", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("+20%% ship quality", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());

        tooltipMakerAPI.addSpacer(10f);
        tooltipMakerAPI.addPara("Affects: fleet", 0f, Misc.getGrayColor(), Misc.getBasePlayerColor(), "fleet");
        tooltipMakerAPI.addSpacer(10f);

        tooltipMakerAPI.addPara("Able to build 1 more permanent hullmod into new ships you custom produce", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void onActivation(SCData data) {
        if (data.getCommander().isPlayer()){
            data.getCommander().getStats().getDynamic().getMod(Stats.CUSTOM_PRODUCTION_MOD).modifyMult(MOD_ID,
                    1f + CUSTOM_PRODUCTION_BONUS / 100f, "Optimized Shipbuilding");
            data.getCommander().getStats().setSkillLevel(MOD_ID, 1);

            Global.getSector().getListenerManager().addListener(this);
        }
    }

    @Override
    public void onDeactivation(SCData data) {
        if (data.getCommander().isPlayer()){
            data.getCommander().getStats().getDynamic().getMod(Stats.CUSTOM_PRODUCTION_MOD).unmodifyMult(MOD_ID);
            data.getCommander().getStats().setSkillLevel(MOD_ID, 0);

            Global.getSector().getListenerManager().removeListener(this);
        }
    }

    @Override
    public void reportEconomyMonthEnd() {
        List<IntelInfoPlugin> prodIntelList = Global.getSector().getIntelManager().getIntel(ProductionReportIntel.class);
        if (prodIntelList.size() > 0) {
            ProductionReportIntel lastProdIntel = (ProductionReportIntel) prodIntelList.get(prodIntelList.size() - 1);
            if (lastProdIntel != null) {
                HackProductionReport hackReport = new HackProductionReport(lastProdIntel);
                MarketAPI gatheringPoint = hackReport.getGatheringPoint();
                CargoAPI local = Misc.getStorageCargo(gatheringPoint);
                ProductionReportIntel.ProductionData productionData = hackReport.getProductionData();
                CargoAPI cargo = productionData.getCargo("Heavy Industry - Custom Production");
                for (FleetMemberAPI member : cargo.getMothballedShips().getMembersListCopy()) {
                    ShipVariantAPI variant = member.getVariant();
                    if (!variant.hasHullMod(HULLMOD_ID)) variant.addPermaMod(HULLMOD_ID);
                }
            }
        }
    }

    @Override
    public void reportEconomyTick(int iterIndex) {

    }

    public static class OptimizedShipbuildingEffect implements MarketSkillEffect {
        public static float SHIP_QUALITY_BONUS = 0.2f;

        @Override
        public void apply(MarketAPI market, String id, float level) {
            market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(id, SHIP_QUALITY_BONUS, "Optimized Shipbuilding (skill)");
        }

        @Override
        public void unapply(MarketAPI market, String id) {
            market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat(id);
        }

        @Override
        public String getEffectDescription(float level) {
            return "+50% maximum value of custom ship and weapon production per month" + "\n" + "+20%% ship quality";
        }

        @Override
        public String getEffectPerLevelDescription() {
            return null;
        }

        @Override
        public ScopeDescription getScopeDescription() {
            return ScopeDescription.GOVERNED_OUTPOST;
        }
    }
}

