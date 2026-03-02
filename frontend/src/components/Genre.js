import React, { useState, useContext } from "react";
import axios from "axios";
import { PlaylistContext } from "../context/PlaylistContext";

const genres = ["Pop", "Rock", "Jazz", "Classical", "Electronic"];

export default function GenreTab() {
  const [selectedGenre, setSelectedGenre] = useState("");
  const [songs, setSongs] = useState([]);
  const [loading, setLoading] = useState(false);

  const { addSongToPlaylist } = useContext(PlaylistContext);

  const fetchSongs = async (genre) => {
    setSelectedGenre(genre);
    setLoading(true);

    try {
      const res = await axios.get(`/api/genres/top-tracks/${genre.toLowerCase()}`);
      setSongs(res.data);
    } catch (err) {
      console.error(err);
      setSongs([]);
    }

    setLoading(false);
  };

  const handleAddToPlaylist = (song) => {
    const playlistName = prompt("Enter playlist name to add this song:");
    if (playlistName) {
      addSongToPlaylist(playlistName, {
        id: song.mbid,
        title: song.title,
        artists: song.artists,
      });
      alert(`Added "${song.title}" to playlist "${playlistName}"`);
    }
  };

  return (
    <div style={{ maxWidth: 800, margin: "20px auto" }}>
      <h2>Genre Explorer</h2>

      {/* Genre Tabs */}
      <div style={{ display: "flex", gap: "10px", marginBottom: "20px", flexWrap: "wrap" }}>
        {genres.map((genre) => (
          <button
            key={genre}
            onClick={() => fetchSongs(genre)}
            style={{
              padding: "8px 16px",
              borderRadius: "20px",
              border: "1px solid #ccc",
              backgroundColor: selectedGenre === genre ? "#007bff" : "#fff",
              color: selectedGenre === genre ? "#fff" : "#000",
              cursor: "pointer",
            }}
          >
            {genre}
          </button>
        ))}
      </div>

      {loading && <p>Loading songs...</p>}

      {!loading && songs.length > 0 && (
        <div style={{ border: "1px solid #ccc", borderRadius: "10px", padding: "10px", backgroundColor: "#fff" }}>
          {songs.map((song) => (
            <div
              key={song.mbid}
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                padding: "8px 10px",
                borderBottom: "1px solid #eee",
              }}
            >
              <span>
                {song.title} — {song.artists.join(", ")}
              </span>
              <button
                onClick={() => handleAddToPlaylist(song)}
                style={{
                  padding: "2px 6px",
                  backgroundColor: "#007bff",
                  color: "#fff",
                  border: "none",
                  borderRadius: "4px",
                  cursor: "pointer",
                }}
              >
                + Playlist
              </button>
            </div>
          ))}
        </div>
      )}

      {!loading && songs.length === 0 && selectedGenre && <p>No songs found for {selectedGenre}</p>}
    </div>
  );
}