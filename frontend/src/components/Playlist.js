import React, { useContext, useState } from "react";
import { PlaylistContext } from "../context/PlaylistContext";
import "./Playlist.css";

const Playlist = () => {
  const {
    playlists,
    recommendations,
    removeSongFromPlaylist,
    addSongToPlaylist,
  } = useContext(PlaylistContext);

  const [activePlaylist, setActivePlaylist] = useState(null);

  const username = "sanjay";

  const togglePlaylist = (name) => {
    setActivePlaylist((prev) => (prev === name ? null : name));
  };

  const handleDelete = (playlistName, songId, songTitle) => {
    if (window.confirm(`Are you sure you want to delete "${songTitle}"?`)) {
      removeSongFromPlaylist(playlistName, songId);
    }
  };

  return (
    <div className="playlist-container">
      <h2>Your Playlists</h2>

      {/* ================= PLAYLIST TABS ================= */}
      <div className="playlist-tabs">
        {playlists.map((playlist) => (
          <button
            key={playlist.name}
            className={`playlist-tab ${
              activePlaylist === playlist.name ? "active" : ""
            }`}
            onClick={() => togglePlaylist(playlist.name)}
          >
            {playlist.name}
          </button>
        ))}
      </div>

      {/* ================= SONGS ================= */}
      {activePlaylist && (
        <div className="songs-dropdown">
          {playlists.find((p) => p.name === activePlaylist)?.songs.length ===
          0 ? (
            <p className="no-songs">No songs added yet.</p>
          ) : (
            <ul className="songs-list">
              {playlists
                .find((p) => p.name === activePlaylist)
                .songs.map((song) => (
                  <li key={song.id} className="song-item">
                    <span>
                      {song.title} —{" "}
                      {Array.isArray(song.artists)
                        ? song.artists.join(", ")
                        : song.artists || "Unknown Artist"}
                    </span>

                    <button
                      className="delete-btn"
                      onClick={() =>
                        handleDelete(activePlaylist, song.id, song.title)
                      }
                    >
                      Delete
                    </button>
                  </li>
                ))}
            </ul>
          )}
        </div>
      )}

      {/* ================= RECOMMENDATIONS ================= */}
      <div style={{ marginTop: "40px" }}>
        <h2>Recommended for You 🔥</h2>

        {recommendations.length === 0 ? (
          <p className="no-songs">No recommendations yet</p>
        ) : (
          <ul className="songs-list">
            {recommendations.map((song) => {

              console.log("SONG FROM BACKEND:", song);

              return (
                <li key={song.id} className="song-item">
                  <div>
                    <div style={{ fontWeight: "600" }}>
                      {song.title}
                    </div>

                    <div style={{ fontSize: "13px", color: "#666" }}>
                      {Array.isArray(song.artists)
                        ? song.artists.join(", ")
                        : song.artists || "Unknown Artist"}
                    </div>
                  </div>

                  <button
                    className="add-btn"
                    onClick={() => {
                      const playlistName = prompt("Add to which playlist?");
                      if (!playlistName) return;

                      addSongToPlaylist(playlistName, song);
                    }}
                  >
                    + Add
                  </button>
                </li>
              );
            })}
          </ul>
        )}
      </div>
    </div>
  );
};

export default Playlist;