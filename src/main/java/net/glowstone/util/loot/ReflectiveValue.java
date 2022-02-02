package net.glowstone.util.loot;

import lombok.Data;
import net.glowstone.util.ReflectionProcessor;

@Data
public class ReflectiveValue<T> {

	private String line;
	private T value;

    public ReflectiveValue(T value) {
        this.value = value;
        this.line = null;
    }

    public ReflectiveValue(String line) {
        this.value = null;
        this.line = line;
    }

    public Object process(Object... context) {
		if(line == null) return value;
		return new ReflectionProcessor(line, context).process();
    }
}
