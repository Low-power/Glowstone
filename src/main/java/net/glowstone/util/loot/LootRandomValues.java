package net.glowstone.util.loot;

import lombok.Data;
import net.glowstone.util.ReflectionProcessor;
import org.bukkit.entity.LivingEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

@Data
public class LootRandomValues {

    private final Integer min, max;
    private final String reflectiveCount;
    private final Map<Integer, Double> probabilities = new HashMap<>();

    public LootRandomValues(int min, int max) {
        this.min = Integer.valueOf(min);
        this.max = Integer.valueOf(max);
        this.reflectiveCount = null;
    }

    public LootRandomValues(JSONObject object) {
        if (!object.containsKey("count")) {
            this.min = null;
            this.max = null;
            this.reflectiveCount = null;
            return;
        }
        Object count = object.get("count");
        if (count instanceof Long) {
            this.min = Integer.valueOf(((Long)count).intValue());
            this.max = min;
            this.reflectiveCount = null;
            return;
        }
        if (count instanceof String) {
            this.min = null;
            this.max = null;
            this.reflectiveCount = (String)count;
            return;
        }
        if (count instanceof JSONArray) {
            this.min = null;
            this.max = null;
            this.reflectiveCount = null;
            
            // todo: probabilities
            return;
        }
        this.reflectiveCount = null;
        object = (JSONObject) count;
        if (object.containsKey("min")) {
            this.min = Integer.valueOf(((Long)object.get("min")).intValue());
        } else {
            this.min = 0;
        }
        this.max = Integer.valueOf(((Long)object.get("max")).intValue());
    }

    /**
     * Selects a random value between min and max, inclusively
     *
     * @param random the random object to generate the number from
     * @return the random value
     */
    public int generate(Random random, LivingEntity entity) {
        if(probabilities == null) {
            double rand = random.nextDouble();
            double cur = 0;
            for (Map.Entry<Integer, Double> entry : probabilities.entrySet()) {
                cur += entry.getValue().doubleValue();
                if (rand < cur) {
                    return entry.getKey().intValue();
                }
            }
            return 0;
        }
        if (reflectiveCount != null) {
            return ((Number) new ReflectionProcessor(reflectiveCount, entity).process()).intValue();
        }
        if (max != null && min != null) {
            if (Objects.equals(min, max)) {
                return min.intValue();
            }
            return random.nextInt(max.intValue() + 1 - min.intValue()) + min.intValue();
        }
        return 0;
    }
}
