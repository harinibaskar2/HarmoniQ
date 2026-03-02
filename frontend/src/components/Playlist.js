import React, { useContext, useState } from "react";
import { PlaylistContext } from "../context/PlaylistContext";
import "./Playlist.css";

const Playlist = () => {
  const { playlists, removeSongFromPlaylist } = useContext(PlaylistContext);
  const [activePlaylist, setActivePlaylist] = useState(null);

  const togglePlaylist = (name) => {
    setActivePlaylist(activePlaylist === name ? null : name);
  };

  const handleDelete = (playlistName, songId, songTitle) => {
    if (window.confirm(`Are you sure you want to delete "${songTitle}" from ${playlistName}?`)) {
      removeSongFromPlaylist(playlistName, songId);
    }
  };

  return (
    <div className="playlist-container">
      <h2>Your Playlists</h2>

      {/* Tabs */}
      <div className="playlist-tabs">
        {playlists.map((playlist) => (
          <button
            key={playlist.name}
            className={`playlist-tab ${activePlaylist === playlist.name ? "active" : ""}`}
            onClick={() => togglePlaylist(playlist.name)}
          >
            {playlist.name}
          </button>
        ))}
      </div>

      {/* Songs Dropdown */}
      {activePlaylist && (
        <div className="songs-dropdown">
          {playlists.find((p) => p.name === activePlaylist)?.songs.length === 0 ? (
            <p className="no-songs">No songs added yet.</p>
          ) : (
            <ul className="songs-list">
              {playlists
                .find((p) => p.name === activePlaylist)
                .songs.map((song) => (
                  <li key={song.id} className="song-item">
                    <span>{song.title} — {song.artists?.join(", ")}</span>
                    <button
                      className="delete-btn"
                      onClick={() => handleDelete(activePlaylist, song.id, song.title)}
                    >
                      Delete
                    </button>
                  </li>
                ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
};

export default Playlist;