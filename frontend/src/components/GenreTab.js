// src/components/GenreTab.js
import React, { useState, useEffect } from "react";

function GenreTab() {
  const [genres, setGenres] = useState([]);
  const [selectedGenre, setSelectedGenre] = useState("");
  const [songs, setSongs] = useState([]);

  // Fetch genres for dropdown
  useEffect(() => {
    fetch("http://localhost:8080/genres")
      .then((res) => res.json())
      .then((data) => setGenres(data))
      .catch((err) => console.error("Failed to fetch genres:", err));
  }, []);

  // Fetch songs whenever a genre is selected
  useEffect(() => {
    if (!selectedGenre) return;

    fetch(
      `http://localhost:8080/songs/genre?genre=${encodeURIComponent(
        selectedGenre
      )}`
    )
      .then((res) => res.json())
      .then((data) => setSongs(data))
      .catch((err) => console.error("Failed to fetch songs:", err));
  }, [selectedGenre]);

  const handleChange = (e) => {
    setSelectedGenre(e.target.value);
  };

  return (
    <div>
      <h2>Genres</h2>
      <select value={selectedGenre} onChange={handleChange}>
        <option value="">Select a genre</option>
        {genres.map((g) => (
          <option key={g} value={g}>
            {g}
          </option>
        ))}
      </select>

      {!selectedGenre && <p>Please select a genre to see songs.</p>}

      {selectedGenre && (
        <div>
          <h3>Songs in "{selectedGenre}"</h3>
          {songs.length === 0 ? (
            <p>No songs found for this genre.</p>
          ) : (
            <ul>
              {songs.map((song) => (
                <li key={song.id}>
                  {song.title} — {song.artists.join(", ")}
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