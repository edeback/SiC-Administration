package xepel.sic_admin.skills.administration;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class CharismaticLeadership extends SCBaseSkillPlugin {

    @Override
    public String getAffectsString() {
        return "governed colonies";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("+10 population growth", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("+20%% accessibility", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void onActivation(SCData data) {
        if (data.getCommander().isPlayer()){
            data.getCommander().getStats().setSkillLevel("sic_admin_charismatic_leadership", 1);
        }
    }

    @Override
    public void onDeactivation(SCData data) {
        if (data.getCommander().isPlayer()){
            data.getCommander().getStats().setSkillLevel("sic_admin_charismatic_leadership", 0);
        }
    }

    public static class CharismaticLeadershipEffect implements MarketSkillEffect, MarketImmigrationModifier {
        public static float POPULATION_GROWTH_BONUS = 10f;
        public static float ACESSIBILITY_BONUS = 0.20f;

        @Override
        public void apply(MarketAPI market, String id, float level) {
            market.addTransientImmigrationModifier(this);
            market.getAccessibilityMod().modifyFlat(id, ACESSIBILITY_BONUS, "Charismatic Leadership (skill)");
        }

        @Override
        public void unapply(MarketAPI market, String id) {
            market.removeTransientImmigrationModifier(this);
            market.getAccessibilityMod().unmodifyFlat(id);
        }

        @Override
        public String getEffectDescription(float level) {
            return "+10 population growth" + "\n" + "+20%% accessibility";
        }

        @Override
        public String getEffectPerLevelDescription() {
            return null;
        }

        @Override
        public ScopeDescription getScopeDescription() {
            return ScopeDescription.GOVERNED_OUTPOST;
        }

        @Override
        public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
            incoming.getWeight().modifyFlat("sic_admin_charismatic_leadership", POPULATION_GROWTH_BONUS, "Charismatic Leadership (skill)");
        }
    }
}
