package com.harmoniq;

import java.util.List;

public class RecommendationService {

    public static UserProfile buildUserProfile(List<Playlist> playlists) {
        UserProfile profile = new UserProfile();

        for (Playlist p : playlists) {
            for (Song s : p.getSongs()) {
                for (String g : s.getGenres()) {
                    profile.addGenre(g);
                }
            }
        }

        return profile;
    }
}

