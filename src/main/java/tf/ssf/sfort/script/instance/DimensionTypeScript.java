package tf.ssf.sfort.script.instance;

import net.minecraft.world.dimension.DimensionType;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;

import java.util.function.Predicate;

public class DimensionTypeScript extends AbstractExtendablePredicateProvider<DimensionType> {

    public DimensionTypeScript() {
        help.put("natural is_natural","Require natural dimension");
        help.put("ultrawarn is_ultrawarm","Require ultra warm dimension");
        help.put("piglin_safe is_piglin_safe","Require piglin safe dimension");
        help.put("bed_working is_bed_working does_bed_work","Require dimension where beds don't blow");
        help.put("respawn_anchor_working is_respawn_anchor_working does_anchor_work","Require dimension where respawn anchors work");
        help.put("skylight has_skylight", "Require dimension to have a skylight");
        help.put("ceiling has_ceiling", "Require dimension to have a ceiling");
        help.put("raids has_raids", "Require dimension to have raids");
        help.put("fixed_time has_fixed_time", "Require dimension to have fixed time");
        help.put("coordinate_scale:double", "Minimum dimension coordinate scale");
    }
    @Override
    public Predicate<DimensionType> getLocalPredicate(String in){
        switch (in){
            case "natural": case "is_natural" : return DimensionType::natural;
            case "ultrawarn": case "is_ultrawarm" : return DimensionType::ultrawarm;
            case "piglin_safe": case "is_piglin_safe" : return DimensionType::piglinSafe;
            case "does_bed_work": case "is_bed_working": case "bed_working" : return DimensionType::bedWorks;
            case "does_anchor_work": case "respawn_anchor_working": case "is_respawn_anchor_working" : return DimensionType::respawnAnchorWorks;
            case "skylight": case "has_skylight" : return DimensionType::hasSkyLight;
            case "ceiling": case "has_ceiling" : return DimensionType::hasCeiling;
            case "raids": case "has_raids" : return DimensionType::hasRaids;
            case "fixed_time": case "has_fixed_time" : return DimensionType::hasFixedTime;
            default : return null;
        }
    }
    @Override
    public Predicate<DimensionType> getLocalPredicate(String in, String val){
        switch (in){
            case "coordinate_scale" : {
                final double arg = Double.parseDouble(val);
                return dim -> dim.coordinateScale() >= arg;
            }
            default : return null;
        }
    }

}