import React, { useState, useContext } from "react";
import { PlaylistContext } from "../context/PlaylistContext";

export default function Discover() {
  const [artist, setArtist] = useState("");
  const [title, setTitle] = useState("");
  const [songs, setSongs] = useState([]);
  const [playlistName, setPlaylistName] = useState("");
  const [loading, setLoading] = useState(false);

  const { addSongToPlaylist } = useContext(PlaylistContext);

  const searchSongs = async () => {
    if (!artist.trim() && !title.trim()) return;
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (artist.trim()) params.append("artist", artist.trim());
      if (title.trim()) params.append("title", title.trim());

      const res = await fetch(`http://localhost:8080/songs?${params.toString()}`);
      if (!res.ok) throw new Error("Failed to fetch songs");

      const data = await res.json();
      setSongs(data);
    } catch (err) {
      console.error(err);
      setSongs([]);
    } finally {
      setLoading(false);
    }
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
      <h2>Discover Songs</h2>

      <div style={{ display: "flex", gap: "10px", marginBottom: "15px" }}>
        <input
          type="text"
          placeholder="Artist (optional)"
          value={artist}
          onChange={(e) => setArtist(e.target.value)}
          style={{ width: "200px" }}
        />
        <input
          type="text"
          placeholder="Song Title (optional)"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
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
        {loading && <p>Loading songs...</p>}
        {!loading && songs.length === 0 && <p>No songs found</p>}

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