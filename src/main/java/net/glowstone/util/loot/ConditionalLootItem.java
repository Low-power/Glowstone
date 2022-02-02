package net.glowstone.util.loot;

import lombok.Data;
import org.json.simple.JSONObject;

@Data
public class ConditionalLootItem {

    private final ProbableValue<String> type;
    private final String condition;
    private final LootRandomValues count;
    private final ProbableValue<Integer> data;
    private final ReflectiveValue<Integer> reflectiveData;

    public ConditionalLootItem(JSONObject object) {
        if (object.containsKey("item")) {
            type = new ProbableValue<>(object, "item");
        } else {
            type = null;
        }
        if (object.containsKey("data")) {
            Object data = object.get("data");
            if (data instanceof String) {
                this.reflectiveData = new ReflectiveValue<Integer>((String) data);
                this.data = null;
            } else {
                this.data = new ProbableValue<>(object, "data");
                this.reflectiveData = null;
            }
        } else {
            data = null;
            reflectiveData = null;
        }
        if (object.containsKey("count")) {
            count = new LootRandomValues(object);
        } else {
            count = null;
        }
        condition = (String) object.get("condition");
    }
}
