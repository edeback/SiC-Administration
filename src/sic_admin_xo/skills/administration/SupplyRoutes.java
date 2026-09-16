package sic_admin_xo.skills.administration;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;
import sic_admin_xo.ColonyProximity;

import java.util.Map;
import java.util.WeakHashMap;

public class SupplyRoutes extends SCBaseSkillPlugin {

    public static String MOD_ID_FUEL = "sic_admin_supply_routes_fuel";
    public static String MOD_ID_SUPPLIES = "sic_admin_supply_routes_supplies";
    public static float MAX_RANGE_LY = 10f;
    public static float MAX_REDUCTION = 0.8f;

    /**
     * Steps the reduction factor is rounded to, as a divisor. The raw factor is a continuous
     * function of distance, so it changes every frame while the fleet moves, and MutableStat
     * allocates a fresh StatMod for every distinct value. 0.5% steps are invisible in the UI and
     * turn a per-ship allocation every frame into one only when the value really moves.
     */
    private static final float QUANTISATION = 200f;

    /** What is currently applied to a fleet, so unchanged frames can skip the whole loop. */
    private static class Applied {
        float factor = Float.NaN;
        int memberCount = -1;
    }

    /**
     * Keyed on the fleet, not the officer, and weakly: this is a cache of what has already been
     * written to that fleet's ships, not skill state. A missing or stale entry costs one redundant
     * pass, and entries go away with the fleet rather than being held alive by this map. The plugin
     * itself is a process-wide singleton (see SCSkillSpec), which is why this is static.
     */
    private static final Map<CampaignFleetAPI, Applied> APPLIED = new WeakHashMap<>();

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
        // Force the next advance() to write the mods rather than trusting a stale cache entry.
        if (data.getFleet() != null) APPLIED.remove(data.getFleet());
    }

    @Override
    public void onDeactivation(SCData data) {
        CampaignFleetAPI fleet = data.getFleet();
        if (fleet == null) return;

        for (FleetMemberAPI ship : fleet.getFleetData().getMembersListCopy()) {
            ship.getStats().getFuelUseMod().unmodifyMult(MOD_ID_FUEL);
            ship.getStats().getSuppliesPerMonth().unmodifyMult(MOD_ID_SUPPLIES);
        }
        APPLIED.remove(fleet);
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

        FleetDataAPI fleetData = fleet.getFleetData();
        int memberCount = fleetData.getNumMembers();

        Applied applied = APPLIED.get(fleet);
        if (applied != null && applied.factor == reductionFactor && applied.memberCount == memberCount) {
            return;
        }

        for (FleetMemberAPI ship : fleetData.getMembersListCopy()) {
            if (reductionFactor < 1f) {
                ship.getStats().getFuelUseMod().modifyMult(MOD_ID_FUEL, reductionFactor, "Supply Routes");
                ship.getStats().getSuppliesPerMonth().modifyMult(MOD_ID_SUPPLIES, reductionFactor, "Supply Routes");
            } else {
                ship.getStats().getFuelUseMod().unmodifyMult(MOD_ID_FUEL);
                ship.getStats().getSuppliesPerMonth().unmodifyMult(MOD_ID_SUPPLIES);
            }
        }

        if (applied == null) {
            applied = new Applied();
            APPLIED.put(fleet, applied);
        }
        applied.factor = reductionFactor;
        applied.memberCount = memberCount;
    }
}
