import React, { useState } from "react";

export default function Genre() {
  const [selectedGenre, setSelectedGenre] = useState("");
  const [songs, setSongs] = useState([]);

  // Dummy songs data
  const allSongs = [
    { id: "1", title: "Song A", artists: ["Artist 1"], genres: ["Pop"] },
    { id: "2", title: "Song B", artists: ["Artist 2"], genres: ["Rock"] },
    { id: "3", title: "Song C", artists: ["Artist 3"], genres: ["Jazz"] },
    { id: "4", title: "Song D", artists: ["Artist 4"], genres: ["Pop", "Rock"] },
    { id: "5", title: "Song E", artists: ["Artist 5"], genres: ["Classical"] },
  ];

  // Predefined genre list
  const genres = ["All", "Pop", "Rock", "Jazz", "Classical"];

  const filterByGenre = (genre) => {
    setSelectedGenre(genre);

    if (genre === "All" || genre === "") {
      setSongs(allSongs);
      return;
    }

    const filtered = allSongs.filter((s) =>
      s.genres.some((g) => g.toLowerCase() === genre.toLowerCase())
    );
    setSongs(filtered);
  };

  return (
    <div>
      <h2>Genre Explorer</h2>

      <div style={{ marginBottom: "20px" }}>
        <select
          value={selectedGenre}
          onChange={(e) => filterByGenre(e.target.value)}
          style={{ padding: "5px 10px", borderRadius: "5px" }}
        >
          {genres.map((g) => (
            <option key={g} value={g}>
              {g}
            </option>
          ))}
        </select>
      </div>

      <div>
        {songs.length === 0 ? (
          <p>No songs found</p>
        ) : (
          songs.map((song) => (
            <div
              key={song.id}
              style={{
                display: "flex",
                justifyContent: "space-between",
                padding: "6px 0",
                borderBottom: "1px solid #eee",
              }}
            >
              <span>
                {song.title} — {song.artists.join(", ")} ({song.genres.join(", ")})
              </span>
            </div>
          ))
        )}
      </div>
    </div>
  );
}