package com.fs.starfarer.api.impl.campaign.intel.misc;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

/**
 * ProductionReportIntel keeps everything interesting in protected fields with no getters.
 * Declaring this class in the same package gets us package-level access to them, for any
 * instance, without reflection.
 */
public class HackProductionReport {

    public static ProductionReportIntel.ProductionData getProductionData(ProductionReportIntel intel) {
        return intel.data;
    }

    public static MarketAPI getGatheringPoint(ProductionReportIntel intel) {
        return intel.gatheringPoint;
    }
}
