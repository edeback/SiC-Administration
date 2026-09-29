package sic_admin_xo.skills.administration;

import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

// Keeps the business_acumen ids from before the rename, since saves store skills by id.
public class Delegation extends SCBaseSkillPlugin {

    @Override
    public String getAffectsString() {
        return "player";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("You can personally administer an additional four colonies without penalty.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void onActivation(SCData data) {
        if (data.getCommander().isPlayer()){
            data.getCommander().getStats().setSkillLevel("sic_admin_business_acumen", 1);
        }
    }

    @Override
    public void onDeactivation(SCData data) {
        if (data.getCommander().isPlayer()){
            data.getCommander().getStats().setSkillLevel("sic_admin_business_acumen", 0);
        }
    }

    public static class DelegationEffect implements CharacterStatsSkillEffect {
        public static float COLONY_NUM_BONUS = 4f;

        @Override
        public void apply(MutableCharacterStatsAPI stats, String id, float level) {
            stats.getOutpostNumber().modifyFlat("sic_admin_business_acumen", COLONY_NUM_BONUS);
        }

        @Override
        public void unapply(MutableCharacterStatsAPI stats, String id) {
            stats.getOutpostNumber().unmodify("sic_admin_business_acumen");
        }

        @Override
        public String getEffectDescription(float level) {
            return "You can personally administer an additional four colonies without penalty.";
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
