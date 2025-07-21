package xepel.sic_admin.skills.administration;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class VerticalIntegration extends SCBaseSkillPlugin {
    public static String MOD_ID_SUPPLY = "sic_admin_vertical_integration_supply";
    public static String MOD_ID_DEMAND = "sic_admin_vertical_integration_demand";
    public static float SUPPLY_BONUS = 1f;
    public static float DEMAND_DISCOUNT = 1f;

    @Override
    public String getAffectsString() {
        return "governed colonies";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("All industries supply 1 more unit of all the commodities they produce", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("All industries demand 1 less unit of all the commodities they require", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void onActivation(SCData data) {
        if (data.getCommander().isPlayer()){
            data.getCommander().getStats().setSkillLevel("sic_admin_vertical_integration", 1);
        }
    }

    @Override
    public void onDeactivation(SCData data) {
        if (data.getCommander().isPlayer()){
            data.getCommander().getStats().setSkillLevel("sic_admin_vertical_integration", 0);
        }
    }

    public static class VerticalIntegrationEffect implements CharacterStatsSkillEffect {

        @Override
        public void apply(MutableCharacterStatsAPI stats, String id, float level) {
            stats.getDynamic().getMod(Stats.SUPPLY_BONUS_MOD).modifyFlat(MOD_ID_SUPPLY, SUPPLY_BONUS);
            stats.getDynamic().getMod(Stats.DEMAND_REDUCTION_MOD).modifyFlat(MOD_ID_DEMAND, DEMAND_DISCOUNT);
        }

        @Override
        public void unapply(MutableCharacterStatsAPI stats, String id) {
            stats.getDynamic().getMod(Stats.SUPPLY_BONUS_MOD).unmodifyFlat(MOD_ID_SUPPLY);
            stats.getDynamic().getMod(Stats.DEMAND_REDUCTION_MOD).unmodifyFlat(MOD_ID_DEMAND);
        }

        @Override
        public String getEffectDescription(float level) {
            return "All industries supply 1 more unit of all the commodities they produce" + "\n" + "All industries demand 1 less unit of all the commodities they require";
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
