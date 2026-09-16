package sic_admin_xo.skills.administration;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.NavBuoyEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;
import sic_admin_xo.ColonyProximity;

public class HyperspaceBuoys extends SCBaseSkillPlugin {

    public static float MAX_RANGE_LY = 10f;
    public static String MOD_ID_NAV = "sic_admin_hyperspace_buoys_nav";
    public static String MOD_ID_SPEED = "sic_admin_hyperspace_buoys_speed";
    public static float NAV_PENALTY = -0.5f;
    public static float NAV_PENALTY_MAKESHIFT = -0.3f;

    @Override
    public String getAffectsString() {
        return "fleet";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("Nav buoys at your colonies affect hyperspace within 10 light-years", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("and reduce hyperspace terrain movement penalties by 50%% (base) / 30%% (makeshift)", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void onActivation(SCData data) {
    }

    @Override
    public void onDeactivation(SCData data) {
        data.getCommander().getStats().getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).unmodifyFlat(MOD_ID_NAV);
    }

    @Override
    public void advance(SCData data, Float amunt) {
        CampaignFleetAPI fleet = data.getFleet();
        if (fleet == null) return;

        int level = ColonyProximity.getBestNavBuoyLevel(fleet, MAX_RANGE_LY);
        if (level > 0) {
            String desc = ((level == 1) ? "Makeshift nav buoy" : "Nav buoy");
            float bonusSpeed = ((level == 1) ?  NavBuoyEntityPlugin.NAV_BONUS_MAKESHIFT :  NavBuoyEntityPlugin.NAV_BONUS);
            float navPenalty = ((level == 1) ? NAV_PENALTY_MAKESHIFT : NAV_PENALTY);

            if (fleet.isInHyperspace()) {
                // Only need to apply speed in hyperspace, otherwise we'll double-add it in-system.
                // Re-applied every frame the way vanilla's own NavBuoyEntityPlugin does: the
                // duration is in days, so letting it lapse would drop the bonus, and now that the
                // level is cached this is a couple of map writes rather than a sector scan.
                fleet.getStats().addTemporaryModFlat(0.1f, MOD_ID_SPEED, desc, bonusSpeed,
                        fleet.getStats().getFleetwideMaxBurnMod());
            }
            data.getCommander().getStats().getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).modifyFlat(MOD_ID_NAV, navPenalty, desc);
        }
        else {
            data.getCommander().getStats().getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).unmodifyFlat(MOD_ID_NAV);
        }
    }
}
