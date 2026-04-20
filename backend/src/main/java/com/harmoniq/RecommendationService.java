package com.harmoniq;

import java.util.List;

// this builds the user profile for each user 


public class RecommendationService {

    public static UserProfile buildUserProfile(List<Playlist> playlists) {
        UserProfile profile = new UserProfile();

        for (Playlist p : playlists) {
            for (Song s : p.getSongs()) {
        
                System.out.println("Song: " + s.getTitle());
                System.out.println("Genres: " + s.getGenres());
        
                for (String g : s.getGenres()) {
                    profile.addGenre(g);
                }
            }
        }

        return profile;
    }
}





