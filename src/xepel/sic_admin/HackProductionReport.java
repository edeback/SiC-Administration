package com.fs.starfarer.api.impl.campaign.intel.misc;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.misc.ProductionReportIntel;

public class HackProductionReport extends ProductionReportIntel {

    public HackProductionReport(MarketAPI gatheringPoint, ProductionData data, int totalCost, int accrued, boolean noProductionThisMonth) {
        super(gatheringPoint, data, totalCost, accrued, noProductionThisMonth);
    }

    public HackProductionReport(ProductionReportIntel intel)
    {
        super(intel.gatheringPoint, intel.data, intel.totalCost, intel.accrued, intel.noProductionThisMonth);
    }

    public ProductionData getProductionData() {
        return data;
    }

    public MarketAPI getGatheringPoint() {
        return gatheringPoint;
    }
}