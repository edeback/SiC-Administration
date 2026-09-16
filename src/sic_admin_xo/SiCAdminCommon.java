package sic_admin_xo;

import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

import java.util.List;

public class SiCAdminCommon {

    /**
     * Best player-owned objective of the given type in one location: 0 = none, 1 = makeshift,
     * 2 = full.
     *
     * Callers that need this across the sector go through {@link ColonyProximity}, which caches it
     * per system. Calling it in a loop over every system on every frame is what this used to do,
     * and it was the mod's single largest campaign cost.
     */
    public static int getLevelForBestEntityInLocation(String typeTag, LocationAPI location)
    {
        int level = 0;
        List<CustomCampaignEntityAPI> entities = location.getCustomEntitiesWithTag(typeTag);
        for (CustomCampaignEntityAPI entity : entities) {
            if (!entity.getFaction().getId().equals(Factions.PLAYER))
                continue;
            if (entity.hasTag(Tags.MAKESHIFT)) {
                level = 1;
            }
            else {
                // Can't get better than this
                return 2;
            }
        }
        return level;
    }

}
