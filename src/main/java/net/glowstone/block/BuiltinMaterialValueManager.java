package net.glowstone.block;

import org.bukkit.Material;
import org.json.simple.JSONValue;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.Map;

public class BuiltinMaterialValueManager implements MaterialValueManager {
    private final Map<Material, BuiltinValueCollection> values;
    private BuiltinValueCollection defaultValue;

    public BuiltinMaterialValueManager() {
        values = new EnumMap<>(Material.class);

		InputStreamReader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("builtin/material_values.json"));
		Map<String, Map<String, ?>> builtin_values = (Map<String, Map<String, ?>>)JSONValue.parse(reader);

        defaultValue = new BuiltinValueCollection(builtin_values.get("default"));
        registerBuiltins(builtin_values);
    }

    private void registerBuiltins(Map<String, ?> obj) {
		Map<String, Map<String, ?>> values_map = (Map<String, Map<String, ?>>)obj.get("values");
		for(Map.Entry<String, Map<String, ?>> entry : values_map.entrySet()) {
			String material_str = entry.getKey();
			Material material = Material.matchMaterial(material_str);
			if (material == null) {
				throw new RuntimeException("Invalid builtin/material_values.json: Couldn't found material: " + material_str);
			}
			values.put(material, new BuiltinValueCollection(entry.getValue()));
		}
    }

    @Override
    public ValueCollection getValues(Material material) {
        if (values.containsKey(material))
            return values.get(material);
        return defaultValue;
    }

    private final class BuiltinValueCollection implements ValueCollection {
        private final Map<String, ?> section;

        BuiltinValueCollection(Map<String, ?> section) {
            this.section = section;
        }

        private Object get(String name) {
            Object got = section.get(name);
			if (got == null) {
				if(this == defaultValue) {
					throw new RuntimeException("No default material value for " + name);
				}
				return defaultValue.get(name);
			}
            return got;
        }

        @Override
        public float getHardness() {
            float hardness = ((Number) get("hardness")).floatValue();
            return hardness == -1 ? Float.MAX_VALUE : hardness;
        }

        @Override
        public float getBlastResistance() {
            return ((Number) get("blastResistance")).floatValue();
        }

        @Override
        public int getLightOpacity() {
            return ((Number) get("lightOpacity")).intValue();
        }

        @Override
        public int getFlameResistance() {
            return ((Number) get("flameResistance")).intValue();
        }

        @Override
        public int getFireResistance() {
            return ((Number) get("fireResistance")).intValue();
        }

        @Override
        public double getSlipperiness() {
            return 0.6;
        }
    }
}
