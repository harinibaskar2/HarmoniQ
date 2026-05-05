import React, { useContext, useState, useEffect } from "react";
import { PlaylistContext } from "../context/PlaylistContext";
import axios from "axios";
import "./Playlist.css";

const API_BASE = "http://localhost:8080";

const Playlist = () => {
  const {
    playlists,
    removeSongFromPlaylist,
    addSongToPlaylist,
  } = useContext(PlaylistContext);

  const [activePlaylist, setActivePlaylist] = useState(null);
  const [recommendations, setRecommendations] = useState([]);

  const username = "sanjay"; // keep MVP simple for now

  // =========================
  // FETCH RECOMMENDATIONS
  // =========================
  const fetchRecommendations = async () => {
    try {
      const res = await axios.get(
        `${API_BASE}/recommendations?username=${username}`
      );

      setRecommendations(res.data);
    } catch (err) {
      console.error("❌ Error fetching recommendations:", err);
    }
  };

  // load recommendations when page opens
  useEffect(() => {
    fetchRecommendations();
  }, [playlists]);

  const togglePlaylist = (name) => {
    setActivePlaylist(activePlaylist === name ? null : name);
  };

  const handleDelete = (playlistName, songId, songTitle) => {
    if (
      window.confirm(
        `Are you sure you want to delete "${songTitle}" from ${playlistName}?`
      )
    ) {
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
                      {song.title} — {song.artists?.join(", ")}
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
{/* ================= RECOMMENDATIONS ================= */}
    <div style={{ marginTop: "40px" }}>
      <h2>Recommended for You 🔥</h2>

      {recommendations.length === 0 ? (
        <p className="no-songs">No recommendations yet</p>
      ) : (
        <ul className="songs-list">
          {recommendations.map((song) => (
            <li key={song.id} className="song-item">

              {/* ✅ CLEAN DISPLAY: ONLY TITLE */}
              <span>
                {song.title}
              </span>

              {/* ➕ ADD BUTTON */}
              <button
                className="add-btn"
                onClick={() => {
                  const playlistName = prompt("Add to which playlist?");

                  if (!playlistName) return;

                  addSongToPlaylist(playlistName, song);

                  // refresh recommendations after adding
                  fetchRecommendations();
                }}
              >
                + Add
              </button>

            </li>
          ))}
        </ul>
      )}
    </div>
    </div>
  );
};

export default Playlist;