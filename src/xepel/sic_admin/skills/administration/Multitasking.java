package xepel.sic_admin.skills.administration;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class Multitasking extends SCBaseSkillPlugin {
    @Override
    public String getAffectsString() {
        return "player";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("You can personally administer two additional colonies without penalty.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void onActivation(SCData data) {
        if (data.getCommander().isPlayer()){
            data.getCommander().getStats().setSkillLevel("sic_admin_multitasking", 1);
        }
    }

    @Override
    public void onDeactivation(SCData data) {
        if (data.getCommander().isPlayer()){
            data.getCommander().getStats().setSkillLevel("sic_admin_multitasking", 0);
        }
    }

    @Override
    public void advance(SCData data, Float amunt) {
        super.advance(data, amunt);
    }

    public static class MultitaskingEffect implements CharacterStatsSkillEffect {

        @Override
        public void apply(MutableCharacterStatsAPI stats, String id, float level) {
            stats.getOutpostNumber().modifyFlat("sic_admin_multitasking", 2);
        }

        @Override
        public void unapply(MutableCharacterStatsAPI stats, String id) {
            stats.getOutpostNumber().unmodify("sic_admin_multitasking");
        }

        @Override
        public String getEffectDescription(float level) {
            return "You can personally administer two additional colonies without penalty.";
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
