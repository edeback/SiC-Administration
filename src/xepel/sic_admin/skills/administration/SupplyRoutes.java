package xepel.sic_admin.skills.administration;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.SensorArrayEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;
import xepel.sic_admin.SiCAdminCommon;

public class SupplyRoutes extends SCBaseSkillPlugin {

    public static String MOD_ID_FUEL = "sic_admin_supply_routes_fuel";
    public static String MOD_ID_SUPPLIES = "sic_admin_supply_routes_supplies";
    public static float MAX_RANGE_LY = 10f;
    public static float MAX_REDUCTION = 0.8f;

    @Override
    public String getAffectsString() {
        return "fleet";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("Your colonies keep you well-supplied and refueled.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("Supply and fuel costs are reduced by up to 80%% depending on how close you are to a colony, " +
                "diminishing to zero at 10 light-years.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        //tooltipMakerAPI.addPara("Supply and fuel costs are reduced by up to 80%% depending on how close you are to a colony,", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        //tooltipMakerAPI.addPara("diminishing to zero at 10 light-years.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void onActivation(SCData data) {
    }

    @Override
    public void onDeactivation(SCData data) {
        for (FleetMemberAPI ship : data.getFleet().getFleetData().getMembersListCopy()) {
            ship.getStats().getSuppliesPerMonth().unmodifyMult(MOD_ID_FUEL);
            ship.getStats().getSuppliesPerMonth().unmodifyMult(MOD_ID_SUPPLIES);
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
        //float lySquared = SiCAdminCommon.lightyearsSquaredToNearestOwnedMarket(data.getFleet().getLocationInHyperspace());
        //float maxSquared = MAX_RANGE_LY * MAX_RANGE_LY;
        //if (lySquared < maxSquared)
        //{
        //    float reductionFactor = 1f - (1f - (lySquared / maxSquared)) * MAX_REDUCTION;
        //    stats.getFuelUseMod().modifyMult(id + "_fuel", reductionFactor, "Supply Routes");
        //    stats.getSuppliesPerMonth().modifyMult(id + "_supplies", reductionFactor, "Supply Routes");
        //}
    }

    @Override
    public void advance(SCData data, Float amunt) {
        // This is horribly inefficient but the only way to keep the values updated
        float lySquared = SiCAdminCommon.lightyearsSquaredToNearestOwnedMarket(data.getFleet().getLocationInHyperspace());
        float maxSquared = MAX_RANGE_LY * MAX_RANGE_LY;
        if (lySquared < maxSquared)
        {
            float reductionFactor = 1f - (1f - (lySquared / maxSquared)) * MAX_REDUCTION;
            for (FleetMemberAPI ship : data.getFleet().getFleetData().getMembersListCopy()) {
                ship.getStats().getFuelUseMod().modifyMult(MOD_ID_FUEL, reductionFactor, "Supply Routes");
                ship.getStats().getSuppliesPerMonth().modifyMult(MOD_ID_SUPPLIES, reductionFactor, "Supply Routes");
            }
        }
        else
        {
            for (FleetMemberAPI ship : data.getFleet().getFleetData().getMembersListCopy()) {
                ship.getStats().getSuppliesPerMonth().unmodifyMult(MOD_ID_FUEL);
                ship.getStats().getSuppliesPerMonth().unmodifyMult(MOD_ID_SUPPLIES);
            }
        }
        //data.getFleet().getStats().addTemporaryModMult(0.1f, MOD_ID, "Supply Routes", 0.9f,
        //        data.getFleet().getStats().getDynamic().getStat(Stats.FUEL_USE_NOT_SHOWN_ON_MAP_MULT));
        //float lySquared = SiCAdminCommon.lightyearsSquaredToNearestOwnedMarket(data.getFleet().getLocationInHyperspace());
        //float maxSquared = MAX_RANGE_LY * MAX_RANGE_LY;
        //if (lySquared < maxSquared)
        //{
        //    float reductionFactor = (1 - lySquared / maxSquared) * MAX_REDUCTION;
        //
        //}
    }
}
