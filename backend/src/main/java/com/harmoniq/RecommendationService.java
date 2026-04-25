package com.harmoniq;

import java.util.ArrayList;
import java.util.List;

public class RecommendationService {

    public static UserProfile buildUserProfile(List<Playlist> playlists) {

        UserProfile profile = new UserProfile();

        for (Playlist p : playlists) {
            for (Song s : p.getSongs()) {

                List<String> artists = s.getArtists();

                if (artists != null) {
                    for (String artist : artists) {
                        profile.addArtist(artist);
                    }
                }
            }
        }

        return profile;
    }

    public static List<Song> recommend(UserProfile profile) {

        List<Song> results = new ArrayList<>();
    
        String topArtist = profile.getTopArtist();
    
        if (topArtist == null) return results;
    
        List<Song> songs =
            MusicBrainzService.fetchSongsByArtistName(topArtist);
    
        results.addAll(songs);
    
        return results;
    }

} 



