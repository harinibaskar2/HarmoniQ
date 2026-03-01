import { createContext, useState } from "react";

export const PlaylistContext = createContext();

export const PlaylistProvider = ({ children }) => {
  const [playlists, setPlaylists] = useState([]); // array of { name, songs: [] }

  const addSongToPlaylist = (playlistName, song) => {
    setPlaylists((prev) => {
      // Find existing playlist
      const existing = prev.find((p) => p.name === playlistName);

      if (existing) {
        // Add song if it doesn't exist already
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

  return (
    <PlaylistContext.Provider value={{ playlists, addSongToPlaylist }}>
      {children}
    </PlaylistContext.Provider>
  );
};

