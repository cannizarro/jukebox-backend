package com.cannizarro.jukebox.config.utils;

import lombok.experimental.UtilityClass;
import java.time.Instant;

@UtilityClass
public class DatabaseUtils {

    public static String generateRowId() {
        return Instant.now().toString() + (long)Math.floor(Math.random()*512);
    }
    public static Instant getInstantFromUUID(final String uuid) {
        return Instant.parse(uuid.substring(0,uuid.indexOf('Z')+1));
    }
}
