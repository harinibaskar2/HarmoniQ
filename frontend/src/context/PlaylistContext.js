import { createContext, useState, useEffect } from "react";
import axios from "axios";

export const PlaylistContext = createContext();

export const PlaylistProvider = ({ children }) => {
  const API_BASE = "http://localhost:8080";

  const username = "sanjay"; // TEMP (as you said for now)

  const [playlists, setPlaylists] = useState([]);
  const [recommendations, setRecommendations] = useState([]);

  // =========================
  // LOAD PLAYLISTS
  // =========================
  const fetchPlaylists = async () => {
    try {
      console.log("🔥 Fetching playlists for:", username);

      const res = await axios.get(`${API_BASE}/playlists`, {
        params: { username },
      });

      setPlaylists(res.data);
    } catch (err) {
      console.error("❌ Error fetching playlists:", err);
    }
  };

  // =========================
  // LOAD RECOMMENDATIONS
  // =========================
  const fetchRecommendations = async () => {
    try {
      console.log("🔥 Fetching recommendations for:", username);

      const res = await axios.get(`${API_BASE}/recommendations`, {
        params: { username },
      });

      console.log("🔥 Recommendations received:", res.data);

      setRecommendations(res.data);
    } catch (err) {
      console.error("❌ Error fetching recommendations:", err);
    }
  };

  // =========================
  // INITIAL LOAD
  // =========================
  useEffect(() => {
    fetchPlaylists();
  }, []);

  // 🔥 refresh recommendations whenever playlists change
  useEffect(() => {
    fetchRecommendations();
  }, [playlists]);

  // =========================
  // ADD SONG
  // =========================
  const addSongToPlaylist = async (playlistName, song) => {
    try {
      console.log("🔥 Adding song:", song);

      const res = await axios.post(`${API_BASE}/playlists/add`, {
        username,
        playlistName,
        song,
      });

      setPlaylists(res.data);
    } catch (err) {
      console.error("❌ Error adding song:", err);
    }
  };

  // =========================
  // REMOVE SONG
  // =========================
  const removeSongFromPlaylist = (playlistName, songId) => {
    setPlaylists((prev) =>
      prev.map((p) =>
        p.name === playlistName
          ? {
              ...p,
              songs: p.songs.filter((song) => song.id !== songId),
            }
          : p
      )
    );
  };

  // =========================
  // CONTEXT VALUE
  // =========================
  return (
    <PlaylistContext.Provider
      value={{
        playlists,
        recommendations,          // 🔥 IMPORTANT
        addSongToPlaylist,
        removeSongFromPlaylist,
        refreshPlaylists: fetchPlaylists,
        refreshRecommendations: fetchRecommendations,
      }}
    >
      {children}
    </PlaylistContext.Provider>
  );
};