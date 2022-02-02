package net.glowstone.entity;

import net.glowstone.entity.meta.MetadataIndex;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.fail;

/**
 * Tests for {@link MetadataIndex}.
 */
public class MetadataIndexTest {

    /**
     * Tests that more specific metadata always appears later.
     */
    @Test
    public void testOrdering() {
        HashMap<Class<?>, MetadataIndex> seen = new HashMap<>();

        for (MetadataIndex index : MetadataIndex.values()) {
            Class<?> clazz = index.getAppliesTo();
            if (clazz == null) {
                continue;
            }

			for(Map.Entry<Class<?>, MetadataIndex> entry : seen.entrySet()) {
				Class<?> key = entry.getKey();
				if(key == clazz) continue;
				if(!clazz.isAssignableFrom(key)) continue;
				fail(String.format("Index %s(%s) follows index %s(%s) which it parents",
					index, clazz.getSimpleName(), entry.getValue(), key.getSimpleName()));
			}

            if (!seen.containsKey(clazz)) {
                seen.put(clazz, index);
            }
        }
    }

    /**
     * Tests that no two MetadataIndex entries can overlap on a single
     * entity. Will not catch failure for entities without any metadata
     * keys defined.
     */
    @Test
    public void testNoOverlap() {
        HashMap<Class<?>, HashMap<Integer, MetadataIndex>> map = new HashMap<>();

        for (MetadataIndex index : MetadataIndex.values()) {
            Class<?> clazz = index.getAppliesTo();
            if (clazz == null) {
                continue;
            }

            if (index == MetadataIndex.ARMORSTAND_LEFT_LEG_POSITION) { //TODO 1.9 - index == MetadataIndex.PLAYER_SKIN_FLAGS || has been removed
                // this index is permitted to override others
                continue;
            }

            // check for duplication
            // check that class is a parent
            // look for matching index
			for(Map.Entry<Class<?>, HashMap<Integer, MetadataIndex>> entry : map.entrySet()) {
				Class<?> key = entry.getKey();
				if(!key.isAssignableFrom(clazz)) continue;
				HashMap<Integer, MetadataIndex> value = entry.getValue();
				if(!value.containsKey(index.getIndex())) continue;
				fail(String.format("Index %s(%s) conflicts with %s(%s)",
					index, clazz.getSimpleName(), value.get(index.getIndex()), key.getSimpleName()));
			}

            // insert this index
            HashMap<Integer, MetadataIndex> classMap = map.get(index.getAppliesTo());
			if(classMap == null) classMap = new HashMap<>();
            classMap.put(index.getIndex(), index);
        }
    }

}
