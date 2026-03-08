// src/components/GenreTab.js
import React, { useState, useEffect, useContext } from "react";
import { PlaylistContext } from "../context/PlaylistContext";

function GenreTab() {
  const [genres, setGenres] = useState([]);
  const [selectedGenre, setSelectedGenre] = useState("");
  const [songs, setSongs] = useState([]);
  const [playlistName, setPlaylistName] = useState("");

  const { addSongToPlaylist } = useContext(PlaylistContext);

  // Fetch genres for dropdown
  useEffect(() => {
    fetch("http://localhost:8080/genres")
      .then((res) => res.json())
      .then((data) => setGenres(data))
      .catch((err) => console.error("Failed to fetch genres:", err));
  }, []);

  // Function to fetch songs by genre
  const fetchSongsByGenre = () => {
    if (!selectedGenre) return;

    fetch(
      `http://localhost:8080/songs/genre?genre=${encodeURIComponent(
        selectedGenre
      )}`
    )
      .then((res) => res.json())
      .then((data) => setSongs(data))
      .catch((err) => console.error("Failed to fetch songs:", err));
  };

  // Fetch songs automatically when genre changes
  useEffect(() => {
    fetchSongsByGenre();
  }, [selectedGenre]);

  const handleChange = (e) => {
    setSelectedGenre(e.target.value);
    setSongs([]);
  };

  const handleAddToPlaylist = (song) => {
    let name = playlistName.trim();

    if (!name) {
      name = prompt("Enter playlist name:");
      if (!name) return;
    }

    addSongToPlaylist(name, song);
    setPlaylistName("");
  };

  return (
    <div>
      <h2>Genres</h2>

      {/* Genre Dropdown */}
      <div style={{ marginBottom: "10px" }}>
        <select value={selectedGenre} onChange={handleChange}>
          <option value="">Select a genre</option>
          {genres.map((g) => (
            <option key={g} value={g}>
              {g}
            </option>
          ))}
        </select>

        <button
          onClick={fetchSongsByGenre}
          style={{ marginLeft: "10px" }}
        >
          Refresh Songs
        </button>
      </div>

      {/* Playlist Input */}
      <input
        type="text"
        placeholder="Playlist name (optional)"
        value={playlistName}
        onChange={(e) => setPlaylistName(e.target.value)}
        style={{ marginBottom: "20px", width: "200px" }}
      />

      {!selectedGenre && <p>Please select a genre to see songs.</p>}

      {selectedGenre && (
        <div>
          <h3>Songs in "{selectedGenre}"</h3>

          {songs.length === 0 ? (
            <p>No songs found for this genre.</p>
          ) : (
            <ul style={{ padding: 0, listStyle: "none" }}>
              {songs.map((song) => (
                <li
                  key={song.id}
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginBottom: "8px",
                    padding: "5px 0",
                    borderBottom: "1px solid #eee",
                  }}
                >
                  <span>
                    {song.title}{" "}
                    {song.artists && song.artists.length > 0
                      ? `- ${song.artists.join(", ")}`
                      : ""}
                  </span>

                  <button onClick={() => handleAddToPlaylist(song)}>
                    Add to Playlist
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}

export default GenreTab;