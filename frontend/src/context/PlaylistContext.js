import { createContext, useState, useEffect } from "react";
import axios from "axios";

export const PlaylistContext = createContext();

export const PlaylistProvider = ({ children }) => {
  const [playlists, setPlaylists] = useState([]);

  // Fetch playlists from backend for a given username
  const fetchPlaylists = async (username) => {
    try {
      const res = await axios.get("http://localhost:8080/playlists", {
        params: { username }, // matches backend query param
      });
      setPlaylists(res.data);
    } catch (err) {
      console.error("Failed to fetch playlists:", err);
    }
  };

  // Add a song to a playlist (also POST to backend)
  const addSongToPlaylist = async (username, playlistName, song) => {
    try {
      await axios.post("http://localhost:8080/playlists/add", {
        username,
        playlistName,
        song,
      });

      // Update local state
      setPlaylists((prev) => {
        const existing = prev.find((p) => p.name === playlistName);
        if (existing) {
          if (!existing.songs.find((s) => s.id === song.id)) {
            return prev.map((p) =>
              p.name === playlistName
                ? { ...p, songs: [...p.songs, song] }
                : p
            );
          }
          return prev;
        } else {
          return [...prev, { name: playlistName, songs: [song] }];
        }
      });
    } catch (err) {
      console.error("Failed to add song to playlist:", err);
    }
  };

  // 🔹 Fetch playlists on mount if user is logged in
  useEffect(() => {
    const username = localStorage.getItem("username"); // or from JWT
    if (username) fetchPlaylists(username);
  }, []);

  return (
    <PlaylistContext.Provider
      value={{ playlists, fetchPlaylists, addSongToPlaylist }}
    >
      {children}
    </PlaylistContext.Provider>
  );
};