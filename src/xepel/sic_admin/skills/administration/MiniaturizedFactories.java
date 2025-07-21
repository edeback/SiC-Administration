package xepel.sic_admin.skills.administration;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class MiniaturizedFactories extends SCBaseSkillPlugin {

    @Override
    public String getAffectsString() {
        return "governed colonies";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("+1 industry slot", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("-20%% colony upkeep cost", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void onActivation(SCData data) {
        if (data.getCommander().isPlayer()){
            data.getCommander().getStats().setSkillLevel("sic_admin_miniaturized_factories", 1);
        }
    }

    @Override
    public void onDeactivation(SCData data) {
        if (data.getCommander().isPlayer()){
            data.getCommander().getStats().setSkillLevel("sic_admin_miniaturized_factories", 0);
        }
    }

    public static class MiniaturizedFactoriesEffect implements MarketSkillEffect {
        public static float UPKEEP_MULT = 0.8f;

        @Override
        public void apply(MarketAPI market, String id, float level) {
            market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).modifyFlat(id, 1f, "Miniaturized Factories (skill)");
            market.getUpkeepMult().modifyMult(id, UPKEEP_MULT, "Miniaturized Factories (skill)");
        }

        @Override
        public void unapply(MarketAPI market, String id) {
            market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodifyFlat(id);
            market.getUpkeepMult().unmodifyMult(id);
        }

        @Override
        public String getEffectDescription(float level) {
            return "+1 industry slot" + "\n" + "-20% colony upkeep cost";
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
