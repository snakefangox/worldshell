package net.snakefangox.worldshell.mixinextras;

import net.snakefangox.worldshell.entity.WorldShellEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Matches based on a proxied entity {@link Predicate} but only returns true for the
 * first result of each worldshell entity. Our tracking setup can sometimes result in duplicate
 * results when searching for entities and this eliminates those.
 */
public class SingleMatchPredicateProxy<T> implements Predicate<T> {

	private final Predicate<T> predicate;
	private final Set<WorldShellEntity> cache = new HashSet<>();

	public SingleMatchPredicateProxy(Predicate<T> predicate) {
		this.predicate = predicate;
	}

	@Override
	public boolean test(T o) {
		if (o instanceof WorldShellEntity) {
			if (cache.contains(o)) return false;
			cache.add((WorldShellEntity) o);
		}
		return predicate.test(o);
	}
}
