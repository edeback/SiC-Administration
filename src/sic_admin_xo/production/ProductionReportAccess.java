package sic_admin_xo.production;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.misc.ProductionReportIntel;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * Reads ProductionReportIntel.data, which is protected and has no getter.
 *
 * This used to be done by declaring a class inside the game's own package, on the theory that
 * package access came with it. It doesn't: the JVM grants package access only between classes
 * that share both the package name and the defining class loader, and a mod jar is always loaded
 * by a different loader from the game API. It threw IllegalAccessError the first time it ran.
 *
 * privateLookupIn is the replacement. Both classes sit in unnamed modules, which are open to
 * everyone, so the JVM grants private access to the target class. It also stays entirely inside
 * java.lang.invoke: Starsector's script class loader refuses to load mod classes that reference
 * java.lang.reflect, and nothing here does.
 */
final class ProductionReportAccess {

    private static final MethodHandle GET_DATA = createGetter();

    private ProductionReportAccess() { }

    /** Null if the field can't be read. A failure is logged rather than thrown: this runs from a
     *  month-end listener, where an exception takes the whole game down. */
    static ProductionReportIntel.ProductionData getProductionData(ProductionReportIntel report) {
        if (GET_DATA == null) return null;
        try {
            return (ProductionReportIntel.ProductionData) GET_DATA.invokeExact(report);
        } catch (Throwable t) {
            Global.getLogger(ProductionReportAccess.class).error("Failed to read a production report", t);
            return null;
        }
    }

    private static MethodHandle createGetter() {
        try {
            return MethodHandles.privateLookupIn(ProductionReportIntel.class, MethodHandles.lookup())
                    .findGetter(ProductionReportIntel.class, "data", ProductionReportIntel.ProductionData.class);
        } catch (Throwable t) {
            Global.getLogger(ProductionReportAccess.class).error(
                    "Can't reach ProductionReportIntel.data; Optimized Shipbuilding will not stamp vanilla custom production", t);
            return null;
        }
    }
}
