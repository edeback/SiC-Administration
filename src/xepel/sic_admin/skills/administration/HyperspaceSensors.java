package xepel.sic_admin.skills.administration;

import com.fs.starfarer.api.impl.campaign.SensorArrayEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;
import xepel.sic_admin.SiCAdminCommon;

public class HyperspaceSensors extends SCBaseSkillPlugin {

    public static float MAX_RANGE_LY = 10f;
    public static String MOD_ID_RANGE = "sic_admin_hyperspace_sensors_range";
    public static String MOD_ID_DETECT = "sic_admin_hyperspace_sensors_detect";
    public static float DETECTED_RANGE_MULT = 0.5f;
    public static float DETECTED_RANGE_MULT_MAKESHIFT = 0.7f;

    @Override
    public String getAffectsString() {
        return "fleet";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("Sensor relays at your colonies affect hyperspace within 10 light-years", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("and reduce detected-at range by 50%% (base) / 30%% (makeshift)", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void onActivation(SCData data) {
    }

    @Override
    public void onDeactivation(SCData data) {
    }

    @Override
    public void advance(SCData data, Float amunt) {
        int level = SiCAdminCommon.getLevelForBestEntityInRange(Tags.SENSOR_ARRAY, data.getFleet(), MAX_RANGE_LY);
        if (level > 0) {
            String desc = ((level == 1) ? "Makeshift sensor array" : "Sensor array");
            float bonusRange = ((level == 1) ?  SensorArrayEntityPlugin.SENSOR_BONUS_MAKESHIFT :  SensorArrayEntityPlugin.SENSOR_BONUS);
            float detectedMult = ((level == 1) ? DETECTED_RANGE_MULT_MAKESHIFT : DETECTED_RANGE_MULT);

            if (data.getFleet().isInHyperspace()) {
                // Only need to apply speed in hyperspace, otherwise we'll double-add it in-system
                data.getFleet().getStats().addTemporaryModFlat(0.1f, MOD_ID_RANGE, desc, bonusRange,
                        data.getFleet().getStats().getSensorRangeMod());
            }
            data.getFleet().getStats().addTemporaryModMult(0.1f, MOD_ID_DETECT, desc, detectedMult,
                    data.getFleet().getStats().getDetectedRangeMod());
        }
    }
}
