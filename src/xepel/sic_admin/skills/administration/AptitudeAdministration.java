package xepel.sic_admin.skills.administration;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import second_in_command.SCData;
import second_in_command.specs.SCAptitudeSection;
import second_in_command.specs.SCBaseAptitudePlugin;

public class AptitudeAdministration extends SCBaseAptitudePlugin {

    @Override
    public void addCodexDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Administration is an end-game logistical aptitude, only useful once you have a few colonies down" +
                " and excelling when you have many. It is focused on supporting those colonies and providing utility when you are near them.",
                0f);
    }

    @Override
    public String getOriginSkillId() {
        return "sc_admin_multitasking";
    }

    @Override
    public void createSections() {
        SCAptitudeSection section1 = new SCAptitudeSection(true, 0, "industry1");
        section1.addSkill("sc_admin_charismatic_leadership");
        section1.addSkill("sc_admin_hyperspace_buoys");
        section1.addSkill("sc_admin_hyperspace_sensors");
        section1.addSkill("sc_admin_supply_routes");
        addSection(section1);

        SCAptitudeSection section2 = new SCAptitudeSection(true, 2, "industry2");
        section2.addSkill("sc_admin_business_acumen");
        section2.addSkill("sc_admin_entrenchment");
        section2.addSkill("sc_admin_optimized_shipbuilding");
        addSection(section2);

        SCAptitudeSection section3 = new SCAptitudeSection(false, 4, "industry4");
        section3.addSkill("sc_admin_vertical_integration");
        section3.addSkill("sc_admin_miniaturized_factories");
        addSection(section3);
    }

    @Override
    public Float getNPCFleetSpawnWeight(SCData scData, CampaignFleetAPI campaignFleetAPI) {
        return 0f;
    }
}
