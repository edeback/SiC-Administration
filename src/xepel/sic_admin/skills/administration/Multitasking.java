package xepel.sic_admin.skills.administration;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
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
            data.getCommander().getStats().getOutpostNumber().modifyFlat("sic_admin_multitasking", 2);
        }
    }

    @Override
    public void onDeactivation(SCData data) {
        if (data.getCommander().isPlayer()){
            data.getCommander().getStats().getOutpostNumber().unmodify("sic_admin_multitasking");
        }
    }

    @Override
    public void advance(SCData data, Float amunt) {
        super.advance(data, amunt);
    }
}
