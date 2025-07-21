package xepel.sic_admin.skills.administration;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.SCUtils;
import second_in_command.specs.SCBaseSkillPlugin;
import second_in_command.specs.SCOfficer;

public class BusinessAcumen extends SCBaseSkillPlugin {

    @Override
    public String getAffectsString() {
        return "governed colonies";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("+25%% income", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void onActivation(SCData data) {
        if (data.getCommander().isPlayer()){
            data.getCommander().getStats().setSkillLevel("sic_admin_business_acumen", 1);
            // DEBUG
            for (SCOfficer officer : SCUtils.getPlayerData().getOfficersInFleet()) {
                officer.addXP(10000000);
            }
        }
    }

    @Override
    public void onDeactivation(SCData data) {
        if (data.getCommander().isPlayer()){
            data.getCommander().getStats().setSkillLevel("sic_admin_business_acumen", 0);
        }
    }

    public static class BusinessAcumenEffect implements MarketSkillEffect {
        public static float INCOME_PERCENT_BONUS = 25f;

        @Override
        public void apply(MarketAPI market, String id, float level) {
            market.getIncomeMult().modifyPercent(id, INCOME_PERCENT_BONUS, "Business Acumen");
        }

        @Override
        public void unapply(MarketAPI market, String id) {
            market.getIncomeMult().unmodifyPercent(id);
        }

        @Override
        public String getEffectDescription(float level) {
            return "+25%% income";
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

