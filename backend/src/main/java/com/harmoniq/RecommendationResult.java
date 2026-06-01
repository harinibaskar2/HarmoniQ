package com.harmoniq;

import java.util.List;

public class RecommendationResult {

    private Song song;
    private List<String> reasons;

    public RecommendationResult(Song song, List<String> reasons) {
        this.song = song;
        this.reasons = reasons;
    }

    public Song getSong() {
        return song;
    }

    public List<String> getReasons() {
        return reasons;
    }
}


