package xepel.sic_admin.skills.administration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class OptimizedShipbuilding extends SCBaseSkillPlugin {

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

            // The production hooks in xepel.sic_admin.production are registered for the whole game
            // and use this skill level to decide whether to build the extra hullmod in.
            data.getCommander().getStats().setSkillLevel(MOD_ID, 1);
        }
    }

    @Override
    public void onDeactivation(SCData data) {
        if (data.getCommander().isPlayer()){
            data.getCommander().getStats().getDynamic().getMod(Stats.CUSTOM_PRODUCTION_MOD).unmodifyMult(MOD_ID);
            data.getCommander().getStats().setSkillLevel(MOD_ID, 0);
        }
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
