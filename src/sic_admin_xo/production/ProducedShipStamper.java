package sic_admin_xo.production;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import sic_admin_xo.skills.administration.OptimizedShipbuilding;

/**
 * Builds the extra permanent hullmod into a freshly produced ship.
 * Shared by every production hook so the different production systems behave identically.
 */
public class ProducedShipStamper {

    /** The hooks are registered for the whole game, so they check the skill at the point of use. */
    public static boolean isActive() {
        if (Global.getSector() == null) return false;
        if (Global.getSector().getPlayerPerson() == null) return false;
        return Global.getSector().getPlayerPerson().getStats().getSkillLevel(OptimizedShipbuilding.MOD_ID) > 0f;
    }

    public static void stamp(FleetMemberAPI member) {
        if (member == null || !isActive()) return;

        ShipVariantAPI variant = member.getVariant();
        if (variant == null) return;

        // Produced ships come out of a FleetInflater with a variant of their own, but don't count
        // on it - writing to a stock variant would modify every ship that shares it.
        if (variant.getSource() != VariantSource.REFIT) {
            variant = variant.clone();
            variant.setSource(VariantSource.REFIT);
            variant.setHullVariantId(Misc.genUID());
            member.setVariant(variant, false, true);
        }

        if (!variant.hasHullMod(OptimizedShipbuilding.HULLMOD_ID)) {
            variant.addPermaMod(OptimizedShipbuilding.HULLMOD_ID);
        }
    }
}
