// PlaylistContext.js
import { createContext, useState } from "react";

export const PlaylistContext = createContext();

export const PlaylistProvider = ({ children }) => {
  const [playlists, setPlaylists] = useState([]); // array of { name, songs: [] }

  // Add a song to a playlist
  const addSongToPlaylist = (playlistName, song) => {
    setPlaylists((prev) => {
      const existing = prev.find((p) => p.name === playlistName);

      if (existing) {
        // Add song if it doesn't already exist
        if (!existing.songs.find((s) => s.id === song.id)) {
          return prev.map((p) =>
            p.name === playlistName
              ? { ...p, songs: [...p.songs, song] }
              : p
          );
        }
        return prev; // song already exists
      } else {
        // Create new playlist
        return [...prev, { name: playlistName, songs: [song] }];
      }
    });
  };

  // Remove a song from a playlist
  const removeSongFromPlaylist = (playlistName, songId) => {
    setPlaylists((prev) =>
      prev.map((p) =>
        p.name === playlistName
          ? { ...p, songs: p.songs.filter((song) => song.id !== songId) }
          : p
      )
    );
  };

  return (
    <PlaylistContext.Provider
      value={{ playlists, addSongToPlaylist, removeSongFromPlaylist }}
    >
      {children}
    </PlaylistContext.Provider>
  );
};