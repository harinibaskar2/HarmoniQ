import { createContext, useState } from "react";

export const PlaylistContext = createContext();

export const PlaylistProvider = ({ children }) => {
  const [playlist, setPlaylist] = useState([]);

  const addSong = (song) => {
    if (!playlist.find((s) => s.id === song.id)) {
      setPlaylist((prev) => [...prev, song]);
    }
  };

  return (
    <PlaylistContext.Provider value={{ playlist, addSong }}>
      {children}
    </PlaylistContext.Provider>
  );
};