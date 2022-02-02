package net.glowstone.util;

import java.util.UUID;

/**
 * Utility methods for dealing with UUIDs.
 */
public final class UuidUtils {

    private UuidUtils() {
    }

    public static UUID fromFlatString(String s) {
		if(s.length() != 32) throw new IllegalArgumentException("string must be 32 characters long");
		StringBuilder sb = new StringBuilder(s);
		// xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
		sb.insert('-', 8).insert('-', 13).insert('-', 18).insert('-', 23);
		return UUID.fromString(sb.toString());
    }

    public static String toFlatString(UUID uuid) {
        return uuid.toString().replace("-", "");
    }
}
