package sic_admin_xo.skills.administration;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;
import sic_admin_xo.ColonyProximity;

public class SupplyRoutes extends SCBaseSkillPlugin {

    public static String MOD_ID_FUEL = "sic_admin_supply_routes_fuel";
    public static String MOD_ID_SUPPLIES = "sic_admin_supply_routes_supplies";
    public static float MAX_RANGE_LY = 10f;
    public static float MAX_REDUCTION = 0.8f;

    /**
     * Steps the reduction factor is rounded to, as a divisor. The raw factor is a continuous
     * function of distance, so it changes every frame while the fleet moves, and MutableStat
     * allocates a fresh StatMod for every distinct value. Rounded, modifyMult() sees the value it
     * already holds and returns after a map lookup. 0.5% steps are invisible in the UI.
     */
    private static final float QUANTISATION = 200f;

    @Override
    public String getAffectsString() {
        return "fleet";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("Your colonies keep you well-supplied and refueled.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("Supply and fuel costs are reduced by up to 80%% depending on how close you are to a colony, " +
                "diminishing to zero at 10 light-years.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void onActivation(SCData data) {
    }

    @Override
    public void onDeactivation(SCData data) {
        CampaignFleetAPI fleet = data.getFleet();
        if (fleet == null) return;

        for (FleetMemberAPI ship : fleet.getFleetData().getMembersListCopy()) {
            ship.getStats().getFuelUseMod().unmodifyMult(MOD_ID_FUEL);
            ship.getStats().getSuppliesPerMonth().unmodifyMult(MOD_ID_SUPPLIES);
        }
    }

    @Override
    public void advance(SCData data, Float amunt) {
        CampaignFleetAPI fleet = data.getFleet();
        if (fleet == null) return;

        float lySquared = ColonyProximity.getNearestColonyLySq(fleet);
        float maxSquared = MAX_RANGE_LY * MAX_RANGE_LY;

        float reductionFactor = 1f;
        if (lySquared < maxSquared) {
            reductionFactor = 1f - (1f - (lySquared / maxSquared)) * MAX_REDUCTION;
            reductionFactor = Math.round(reductionFactor * QUANTISATION) / QUANTISATION;
        }

        // This has to visit every ship every frame, even when the factor hasn't changed.
        // FleetMember.updateStats() replaces the member's stats object wholesale and rebuilds it
        // from hullmods and skills, so anything applied from outside that pipeline is silently
        // dropped on refit, docking, officer changes and so on. Skipping "unchanged" frames means
        // the discount vanishes the first time that happens and never comes back while the
        // factor holds steady, which is exactly the in-system case. With the factor rounded,
        // modifyMult() on an intact stat is a map lookup and a float compare, no allocation.
        for (FleetMemberAPI ship : fleet.getFleetData().getMembersListCopy()) {
            if (reductionFactor < 1f) {
                ship.getStats().getFuelUseMod().modifyMult(MOD_ID_FUEL, reductionFactor, "Supply Routes");
                ship.getStats().getSuppliesPerMonth().modifyMult(MOD_ID_SUPPLIES, reductionFactor, "Supply Routes");
            } else {
                ship.getStats().getFuelUseMod().unmodifyMult(MOD_ID_FUEL);
                ship.getStats().getSuppliesPerMonth().unmodifyMult(MOD_ID_SUPPLIES);
            }
        }
    }
}
