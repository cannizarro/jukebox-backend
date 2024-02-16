package com.cannizarro.jukebox.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessages {
    public static final String NO_PLAYBACK_EXCEPTION = "No playback is detected. Please play any song through spotify on the device which is responsible for controlling your jukebox.";
    public static final String ONLY_TRACK_TYPE_EXCEPTION = "Only music is supported for this application. Spotify might be playing something else other than songs, for example a podcast.";
    public static final String DEVICE_RESTRICTED_EXCEPTION = "The device which is currently playing music is restricted for any outside control other that spotify. Please fix this or play music on a device which is not restricted";
    public static final String SPOTIFY_ACTION_NOT_ALLOWED = "Action is not allowed";
    public static final String NO_PLAYBACK_EXCEPTION_FOR_CUSTOMER = "No playback is detected. Please ask the staff to play any song just to be online.";
}
