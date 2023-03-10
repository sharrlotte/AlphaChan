package AlphaChan.main.data.mindustry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum SchematicTag {
    SERPULO, EREKIR,
    // Resource
    COPPER, LEAD, COAL, SCRAP, GRAPHITE, METAGLASS, SILICON, SPORE_POD, TITANIUM, PLASTANIUM, PYRATITE, BLAST_COMPOUND,
    THORIUM, PHASE_FABRIC, SURGE_ALLOY, BERYLIUM, TUNGSTEN, OXIDE, CARDIBE,
    // Liquid
    WATER, SLAG, OIL, CRYOFLUID, NEOPLASM, ARKYCITE, OZONE, HYDROGEN, NITROGEN, CYANOGEN,
    // Type
    UNIT, LOGIC, RESOURCE, DEFENSE, J4F, POWER, KICKSTART, SPAM,
    // Placement
    ON_CORE, REMOTE, ON_ORE, MASS_DRIVER,
    // Unit
    TIER1, TIER2, TIER3, TIER4, TIER5,
    // Mode
    CAMPAIGN, PVP, ATTACK, SANDBOX, HEX,
    // Factory
    GRAPHITE_PRESS, MULTI_PRESS, SILICON_SMELTER, SILICON_CRUCIBLE, SEPARATOR, DISASSEMBLER, SILICON_ARC,
    // POWER
    COMBUSTION, THERMAL, STEAM, DIFFERENTIAL, RTG, SOLAR, THORIUM_REACTOR, IMPACT_REACTOR, TURBINE_CONDENSER,
    CHEMICAL_COMBUSTION, PYROLYSIS, FLUX_REACTOR, NEOPLASIA_REACTOR,
    // Utils
    HEAT, OD, SHIELD, ALL_IN_ONE,
    // Size
    MINI, SMALL, MEDIUM, LARGE, HUGE;

    public static List<String> getTags() {
        List<String> a = new ArrayList<String>();
        List<SchematicTag> temp = Arrays.asList(SchematicTag.values());
        temp.forEach(t -> a.add(t.name()));
        return a;
    }
}
