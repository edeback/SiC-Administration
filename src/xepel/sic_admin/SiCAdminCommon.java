package xepel.sic_admin;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.misc.HypershuntIntel;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SiCAdminCommon {

    public static float lightyearsSquaredToNearestOwnedMarket(Vector2f playerLocation)
    {
        float minDistanceSquared = Float.MAX_VALUE;
        List<MarketAPI> allMarkets = Global.getSector().getEconomy().getMarketsCopy();
        for (MarketAPI market : allMarkets)  {
            if (market.getFactionId().equals(Factions.PLAYER)) {
                float distSquared = Misc.getDistanceSq(playerLocation, market.getLocationInHyperspace());
                if (distSquared < minDistanceSquared) minDistanceSquared = distSquared;
            }
        }
        minDistanceSquared = minDistanceSquared / Misc.getUnitsPerLightYear() / Misc.getUnitsPerLightYear();
        return minDistanceSquared;
    }

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

    public static int getLevelForBestEntityInRange(String typeTag, CampaignFleetAPI playerFleet, float maxDistLY)
    {
        int level = 0;
        if (!playerFleet.isInHyperspace()) {
            // Check local first
            level = getLevelForBestEntityInLocation(typeTag, playerFleet.getContainingLocation());
            // If we're maxed out, no need to check anywhere else
            if (level == 2) return 2;
        }
        float maxDistSquaredUnits = maxDistLY * maxDistLY * Misc.getUnitsPerLightYear() * Misc.getUnitsPerLightYear();
        Set<LocationAPI> results = new HashSet<>();
        List<MarketAPI> allMarkets = Global.getSector().getEconomy().getMarketsCopy();
        for (MarketAPI market : allMarkets)
        {
            if (market.getFactionId().equals(Factions.PLAYER))
                results.add(market.getContainingLocation());
        }
        for (LocationAPI location : results) {
            if (location instanceof StarSystemAPI) {
                if (Misc.getDistanceSq(location.getLocation(), playerFleet.getLocation()) <= maxDistSquaredUnits) {
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
                }
            }
        }
        return level;
    }

}
