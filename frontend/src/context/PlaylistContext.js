import { createContext, useState, useEffect } from "react";
import axios from "axios";

export const PlaylistContext = createContext();

export const PlaylistProvider = ({ children }) => {
  const [playlists, setPlaylists] = useState([]);

  const username = "sanjay"; // TEMP

  const API_BASE = "http://localhost:8080"; // 🔥 IMPORTANT FIX

  // =========================
  // LOAD PLAYLISTS (BACKEND)
  // =========================
  const fetchPlaylists = async () => {
    try {
      console.log("🔥 Fetching playlists for:", username);

      const res = await axios.get(`${API_BASE}/playlists`, {
        params: { username },
      });

      console.log("🔥 Playlists received:", res.data);

      setPlaylists(res.data);
    } catch (err) {
      console.error("❌ Error fetching playlists:", err);
    }
  };

  useEffect(() => {
    fetchPlaylists();
  }, []);

  // =========================
  // ADD SONG (BACKEND)
  // =========================
  const addSongToPlaylist = async (playlistName, song) => {
    console.log("🔥 FRONTEND addSongToPlaylist CALLED");
    console.log("playlist:", playlistName);
    console.log("song:", song);

    try {
      const res = await axios.post(`${API_BASE}/playlists/add`, {
        username,
        playlistName,
        song,
      });

      console.log("🔥 Backend response:", res.data);

      setPlaylists(res.data);
    } catch (err) {
      console.error("❌ Error adding song:", err);
    }
  };

  // =========================
  // REMOVE SONG (LOCAL ONLY)
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

  return (
    <PlaylistContext.Provider
      value={{
        playlists,
        addSongToPlaylist,
        removeSongFromPlaylist,
        refreshPlaylists: fetchPlaylists,
      }}
    >
      {children}
    </PlaylistContext.Provider>
  );
};