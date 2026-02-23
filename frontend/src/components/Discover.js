// Discover.js
import React, { useState } from "react";

export default function Discover({ onAddToPlaylist }) {
  const [query, setQuery] = useState("");
  const [artist, setArtist] = useState("");
  const [genre, setGenre] = useState("");
  const [songs, setSongs] = useState([]);
  const [playlistName, setPlaylistName] = useState("");

  const searchSongs = async () => {
    if (!query && !artist && !genre) return;
    try {
      const params = new URLSearchParams();
      if (query) params.append("query", query);
      if (artist) params.append("artist", artist);
      if (genre) params.append("genre", genre);

      const res = await fetch(`http://localhost:8080/songs?${params.toString()}`);
      if (!res.ok) throw new Error("Failed to fetch songs");
      const data = await res.json();
      setSongs(data);
    } catch (err) {
      console.error(err);
      setSongs([]);
    }
  };

  const handleAddToPlaylist = (song) => {
    let name = playlistName.trim();
    if (!name) {
      name = prompt("Enter playlist name:");
      if (!name) return;
    }
    onAddToPlaylist(name, song); 
    setPlaylistName(""); 
  };

  return (
    <div>
      <h2>Discover Songs</h2>
      <div style={{ display: "flex", gap: "10px", marginBottom: "15px" }}>
        <input
          type="text"
          placeholder="Song title"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          style={{ width: "200px" }}
        />
        <input
          type="text"
          placeholder="Artist"
          value={artist}
          onChange={(e) => setArtist(e.target.value)}
          style={{ width: "200px" }}
        />
        <input
          type="text"
          placeholder="Genre (e.g., pop, country)"
          value={genre}
          onChange={(e) => setGenre(e.target.value)}
          style={{ width: "200px" }}
        />
        <button onClick={searchSongs}>Search</button>
      </div>

      <input
        type="text"
        placeholder="Playlist name (optional)"
        value={playlistName}
        onChange={(e) => setPlaylistName(e.target.value)}
        style={{ marginBottom: "20px", width: "200px" }}
      />

      <div>
        {songs.length === 0 && <p>No songs found</p>}
        {songs.map((song) => (
          <div
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
              {song.title} {song.artists && song.artists.length > 0 ? `- ${song.artists.join(", ")}` : ""}
            </span>
            <button onClick={() => handleAddToPlaylist(song)}>Add to Playlist</button>
          </div>
        ))}
      </div>
    </div>
  );
}


