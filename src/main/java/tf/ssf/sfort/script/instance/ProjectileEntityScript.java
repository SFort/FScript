package tf.ssf.sfort.script.instance;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;
import tf.ssf.sfort.script.util.DefaultParsers;

import java.util.function.Predicate;

public class ProjectileEntityScript<T extends ProjectileEntity> extends AbstractExtendablePredicateProvider<T> {

	public ProjectileEntityScript() {
		help.put("~owner projectile_owner:ENTITY", "Require owner of projectile to match");
	}

	@Override
	public Predicate<T> getLocalEmbed(String in, String script){
		switch (in) {
			case "owner" : case "projectile_owner" : {
				final Predicate<Entity> predicate = DefaultParsers.ENTITY_PARSER.parse(script);
				if (predicate == null) return null;
				return entity -> predicate.test(entity.getOwner());
			}
			default : return null;
		}
	}
}
