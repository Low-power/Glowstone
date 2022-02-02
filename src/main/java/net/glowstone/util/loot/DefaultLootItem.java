package net.glowstone.util.loot;

import lombok.Data;
import org.json.simple.JSONObject;

@Data
public class DefaultLootItem {

    private final LootRandomValues count;
    private final ProbableValue<String> type;
    private final ProbableValue<Integer> data;
    private final ReflectiveValue<Integer> reflectiveData;

    public DefaultLootItem(JSONObject object) {
        this.type = new ProbableValue<>(object, "item");
        this.count = new LootRandomValues(object);
        if (object.containsKey("data")) {
			Object data = object.get("data");
            if(data instanceof String) {
                this.reflectiveData = new ReflectiveValue<Integer>((String)data);
                this.data = null;
            } else if(data instanceof Long) {
				Integer value = Integer.valueOf(((Long)data).intValue());
                this.reflectiveData = new ReflectiveValue<Integer>(value);
                this.data = null;
            } else {
                this.reflectiveData = null;
                this.data = new ProbableValue<>(object, "data");
            }
        } else {
            this.reflectiveData = new ReflectiveValue<>(0);
            this.data = null;
        }
    }
}
