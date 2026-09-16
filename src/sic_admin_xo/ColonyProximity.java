package sic_admin_xo;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shared, interval-refreshed answers to how close the fleet is to its colonies.
 *
 * The three fleet-scoped skills all run from SCData.advance(), which is every frame and also runs
 * while the game is paused. Without this cache each of them walked the whole economy every frame:
 * in a modded sector that is hundreds of markets per skill per frame, plus a defensive list copy
 * each time.
 *
 * Two cadences. Which systems the player owns, and which objectives sit in them, changes on the
 * order of campaign days. The fleet's distance to them changes continuously but slowly. Both gates
 * are on campaign time rather than frames, so a paused game does no work at all after the first
 * call, and a fast-forwarding one refreshes at the same in-game rate as a slow one.
 *
 * Ranges are not baked into the cache. Instead it records the distance at which each objective
 * grade becomes available, so a range query is a pair of float comparisons and each skill keeps
 * ownership of its own radius constant.
 */
public class ColonyProximity {

    /** How often the colony snapshot is rebuilt. This is the call that touches every market. */
    private static final float COLONY_REFRESH_DAYS = 1f;
    /** How often distances are recomputed. Roughly 0.2s of real time at normal speed. */
    private static final float DISTANCE_REFRESH_DAYS = 0.02f;

    /** One player-owned star system, with the best objective of each type already resolved. */
    private static class ColonySystem {
        Vector2f hyperLoc;
        int navBuoyLevel;
        int sensorArrayLevel;
    }

    private static final List<ColonySystem> colonySystems = new ArrayList<>();
    /** Hyperspace positions of every player market. */
    private static final List<Vector2f> colonyHyperLocs = new ArrayList<>();

    private static boolean hasSnapshot = false;
    private static boolean hasDistances = false;
    private static long lastColonyRefresh;
    private static long lastDistanceRefresh;
    /**
     * Whose distances are currently cached. Held as an id rather than a reference so a despawned
     * fleet is not kept alive by this class. In practice only the player fleet ever gets these
     * skills (the aptitude's NPC spawn weight is 0), but the cache is shared between three skills
     * and silently answering for the wrong fleet would be a nasty way to find that out.
     */
    private static String lastFleetId = null;

    private static float nearestColonyLySq = Float.MAX_VALUE;
    private static float navBuoyFullLySq = Float.MAX_VALUE;
    private static float navBuoyMakeshiftLySq = Float.MAX_VALUE;
    private static float sensorArrayFullLySq = Float.MAX_VALUE;
    private static float sensorArrayMakeshiftLySq = Float.MAX_VALUE;

    /**
     * Drops everything. Called from onGameLoad: the cached timestamps belong to the sector that
     * produced them, and comparing them against a different save's clock is meaningless.
     */
    public static void reset() {
        colonySystems.clear();
        colonyHyperLocs.clear();
        hasSnapshot = false;
        hasDistances = false;
        lastFleetId = null;
        nearestColonyLySq = Float.MAX_VALUE;
        navBuoyFullLySq = Float.MAX_VALUE;
        navBuoyMakeshiftLySq = Float.MAX_VALUE;
        sensorArrayFullLySq = Float.MAX_VALUE;
        sensorArrayMakeshiftLySq = Float.MAX_VALUE;
    }

    /** Squared distance in light-years to the nearest player-owned market. */
    public static float getNearestColonyLySq(CampaignFleetAPI fleet) {
        refresh(fleet);
        return nearestColonyLySq;
    }

    /** 0 = none in range, 1 = makeshift, 2 = full. */
    public static int getBestNavBuoyLevel(CampaignFleetAPI fleet, float maxRangeLy) {
        refresh(fleet);
        float maxSq = maxRangeLy * maxRangeLy;
        if (navBuoyFullLySq <= maxSq) return 2;
        if (navBuoyMakeshiftLySq <= maxSq) return 1;
        return 0;
    }

    /** 0 = none in range, 1 = makeshift, 2 = full. */
    public static int getBestSensorArrayLevel(CampaignFleetAPI fleet, float maxRangeLy) {
        refresh(fleet);
        float maxSq = maxRangeLy * maxRangeLy;
        if (sensorArrayFullLySq <= maxSq) return 2;
        if (sensorArrayMakeshiftLySq <= maxSq) return 1;
        return 0;
    }

    private static void refresh(CampaignFleetAPI fleet) {
        if (fleet == null || Global.getSector() == null) return;

        boolean sameFleet = fleet.getId() != null && fleet.getId().equals(lastFleetId);
        if (hasDistances && sameFleet
                && Global.getSector().getClock().getElapsedDaysSince(lastDistanceRefresh) < DISTANCE_REFRESH_DAYS) {
            return;
        }
        lastDistanceRefresh = Global.getSector().getClock().getTimestamp();
        lastFleetId = fleet.getId();
        hasDistances = true;

        if (!hasSnapshot
                || Global.getSector().getClock().getElapsedDaysSince(lastColonyRefresh) >= COLONY_REFRESH_DAYS) {
            rebuildColonySnapshot();
            lastColonyRefresh = Global.getSector().getClock().getTimestamp();
            hasSnapshot = true;
        }

        Vector2f hyperLoc = fleet.getLocationInHyperspace();
        float unitsPerLy = Misc.getUnitsPerLightYear();
        float lySqPerUnitSq = 1f / (unitsPerLy * unitsPerLy);

        nearestColonyLySq = Float.MAX_VALUE;
        for (Vector2f loc : colonyHyperLocs) {
            float distSq = Misc.getDistanceSq(hyperLoc, loc) * lySqPerUnitSq;
            if (distSq < nearestColonyLySq) nearestColonyLySq = distSq;
        }

        navBuoyFullLySq = Float.MAX_VALUE;
        navBuoyMakeshiftLySq = Float.MAX_VALUE;
        sensorArrayFullLySq = Float.MAX_VALUE;
        sensorArrayMakeshiftLySq = Float.MAX_VALUE;

        // Objectives in the system the fleet is sitting in count at zero distance, whether or not
        // the player has a colony there.
        if (!fleet.isInHyperspace() && fleet.getContainingLocation() != null) {
            recordNavBuoy(SiCAdminCommon.getLevelForBestEntityInLocation(
                    Tags.NAV_BUOY, fleet.getContainingLocation()), 0f);
            recordSensorArray(SiCAdminCommon.getLevelForBestEntityInLocation(
                    Tags.SENSOR_ARRAY, fleet.getContainingLocation()), 0f);
        }

        for (ColonySystem cs : colonySystems) {
            float distSq = Misc.getDistanceSq(hyperLoc, cs.hyperLoc) * lySqPerUnitSq;
            recordNavBuoy(cs.navBuoyLevel, distSq);
            recordSensorArray(cs.sensorArrayLevel, distSq);
        }
    }

    private static void recordNavBuoy(int level, float distSq) {
        if (level >= 2) {
            if (distSq < navBuoyFullLySq) navBuoyFullLySq = distSq;
        } else if (level == 1 && distSq < navBuoyMakeshiftLySq) {
            navBuoyMakeshiftLySq = distSq;
        }
    }

    private static void recordSensorArray(int level, float distSq) {
        if (level >= 2) {
            if (distSq < sensorArrayFullLySq) sensorArrayFullLySq = distSq;
        } else if (level == 1 && distSq < sensorArrayMakeshiftLySq) {
            sensorArrayMakeshiftLySq = distSq;
        }
    }

    /** The only place that walks the economy. Runs about once a campaign day. */
    private static void rebuildColonySnapshot() {
        colonySystems.clear();
        colonyHyperLocs.clear();

        Set<LocationAPI> seenSystems = new HashSet<>();
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            // isPlayerOwned() rather than a Factions.PLAYER id check: with Nexerelin the player
            // normally runs a custom faction id, and an id comparison misses all of those colonies.
            if (!market.isPlayerOwned()) continue;

            // getLocationInHyperspace() resolves to the containing location's position, so every
            // market in a system shares one point and this list stays short in practice.
            colonyHyperLocs.add(market.getLocationInHyperspace());

            LocationAPI loc = market.getContainingLocation();
            if (!(loc instanceof StarSystemAPI) || !seenSystems.add(loc)) continue;

            ColonySystem cs = new ColonySystem();
            cs.hyperLoc = ((StarSystemAPI) loc).getLocation();
            cs.navBuoyLevel = SiCAdminCommon.getLevelForBestEntityInLocation(Tags.NAV_BUOY, loc);
            cs.sensorArrayLevel = SiCAdminCommon.getLevelForBestEntityInLocation(Tags.SENSOR_ARRAY, loc);
            colonySystems.add(cs);
        }
    }
}
