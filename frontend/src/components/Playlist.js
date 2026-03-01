import React, { useContext, useState } from "react";
import { PlaylistContext } from "../context/PlaylistContext";

const Playlist = () => {
  const { playlists } = useContext(PlaylistContext);
  const [selectedPlaylist, setSelectedPlaylist] = useState("");

  const current = playlists.find((p) => p.name === selectedPlaylist);

  return (
    <div>
      <h2>Your Playlists</h2>

      {/* Playlist selector */}
      <select
        value={selectedPlaylist || ""}
        onChange={(e) => setSelectedPlaylist(e.target.value)}
        style={{ padding: "5px 10px", borderRadius: "5px", marginBottom: "20px" }}
      >
        <option value="">-- Select Playlist --</option>
        {playlists.map((p) => (
          <option key={p.name} value={p.name}>
            {p.name}
          </option>
        ))}
      </select>

      {selectedPlaylist && (
        <>
          {current && current.songs.length === 0 ? (
            <p>No songs added yet.</p>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
              {current.songs.map((song) => (
                <div
                  key={song.id}
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    padding: "5px 0",
                    borderBottom: "1px solid #333",
                  }}
                >
                  <span>{song.title} — {song.artists?.join(", ")}</span>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default Playlist;