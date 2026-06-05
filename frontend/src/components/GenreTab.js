import React, { useState, useEffect, useContext } from "react";
import { PlaylistContext } from "../context/PlaylistContext";


/**



 /**
  * Temporary hardcoded username used to simulate an authenticated user during development.
  *
  * This enables testing of user-specific recommendation features. In a production application, this would be replaced with actual authentication logic to determine the logged-in user.
 * GenreTab component provides a genre-based music discovery and recommendation interface.

 */






function GenreTab() {

  const [genres, setGenres] = useState([]);
  const [selectedGenre, setSelectedGenre] = useState("");
  const [songs, setSongs] = useState([]);
  const [playlistName, setPlaylistName] = useState("");
  const [loading, setLoading] = useState(false);

  const [recommendedSongs, setRecommendedSongs] = useState([]);
  const [topGenres, setTopGenres] = useState([]);

  const { addSongToPlaylist } = useContext(PlaylistContext);

  const username = "sanjay";

  // =========================
  // FETCH GENRES
  // =========================
  useEffect(() => {
    fetch("http://localhost:8080/genres")
      .then((res) => res.json())
      .then((data) => setGenres(data))
      .catch((err) => console.error(err));
  }, []);

  // =========================
  // FETCH SONGS BY GENRE
  // =========================
  const fetchSongsByGenre = () => {

    if (!selectedGenre) return;

    setLoading(true);

    fetch(
      `http://localhost:8080/songs/genre?genre=${encodeURIComponent(selectedGenre)}`
    )
      .then((res) => res.json())
      .then((data) => setSongs(data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  };

  // =========================
  // FETCH RECOMMENDATIONS
  // =========================
  const fetchRecommendations = () => {

    fetch(`http://localhost:8080/recommendations/genre?username=${username}`)
      .then((res) => res.json())
      .then((data) => {
        setTopGenres(data.topGenres || []);
        setRecommendedSongs(data.songs || []);
      })
      .catch((err) =>
        console.error(" recommendation error:", err)
      );
  };

  useEffect(() => {
    fetchRecommendations();
  }, []);

  // =========================
  // FEEDBACK
  // =========================
  const sendFeedback = async (song, type) => {

    try {
      await fetch("http://localhost:8080/feedback/genre-song", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          username,
          genre: selectedGenre,
          songId: song.id,
          type
        }),
      });

      fetchRecommendations();

    } catch (err) {
      console.error(err);
    }
  };

  // =========================
  // ADD TO PLAYLIST
  // =========================
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

      {/* ================= GENRE SELECT ================= */}
      <select
        value={selectedGenre}
        onChange={(e) => setSelectedGenre(e.target.value)}
      >
        <option value="">Select a genre</option>
        {genres.map((g) => (
          <option key={g} value={g}>
            {g}
          </option>
        ))}
      </select>

      <button onClick={fetchSongsByGenre} style={{ marginLeft: "10px" }}>
        Refresh Songs
      </button>

      <input
        type="text"
        placeholder="Playlist name (optional)"
        value={playlistName}
        onChange={(e) => setPlaylistName(e.target.value)}
        style={{ marginLeft: "10px" }}
      />

      {/* ================= SONG LIST ================= */}
      <div style={{ marginTop: "20px" }}>

        <h3>🎧 Songs in "{selectedGenre}"</h3>

        {loading ? (
          <p>Loading...</p>
        ) : songs.length === 0 ? (
          <p>No songs found.</p>
        ) : (
          <ul style={{ listStyle: "none", padding: 0 }}>

            {songs.map((song) => (
              <li
                key={song.id}
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  padding: "10px",
                  marginBottom: "10px",
                  borderRadius: "10px",
                  backgroundColor: "#111",
                  border: "1px solid #333"
                }}
              >
                <span>
                  {song.title}
                  {song.artists?.length > 0
                    ? ` - ${song.artists.join(", ")}`
                    : ""}
                </span>

                <div>
                  <button onClick={() => sendFeedback(song, "like")}>👍</button>
                  <button onClick={() => sendFeedback(song, "dislike")}>👎</button>
                  <button onClick={() => handleAddToPlaylist(song)}>Add</button>
                </div>
              </li>
            ))}

          </ul>
        )}
      </div>

      {/* ================= RECOMMENDATIONS ================= */}
      <div style={{ marginTop: "40px" }}>

        <h3> Recommended For You</h3>

        {/* ================= CLICKABLE GENRE BUTTONS ================= */}
        <div style={{ display: "flex", gap: "8px", marginBottom: "10px" }}>
          {topGenres.map((g) => (
            <button
              key={g}
              onClick={() => console.log("Clicked genre:", g)}
              style={{
                padding: "6px 12px",
                borderRadius: "20px",
                backgroundColor: "#ed859f",
                color: "#fff",
                fontSize: "12px",
                border: "none",
                cursor: "pointer"
              }}
            >
              {g}
            </button>
          ))}
        </div>

        {/* ================= DESCRIPTION ================= */}
        {topGenres.length > 0 ? (
          <p>
            Because you like{" "}
            <b>{topGenres.join(", ")}</b>
          </p>
        ) : (
          <p>Like songs to personalize recommendations</p>
        )}

        {/* ================= SONGS ================= */}
        {recommendedSongs.length === 0 ? (
          <p>No recommendations yet</p>
        ) : (
          <ul style={{ listStyle: "none", padding: 0 }}>

            {recommendedSongs.map((song) => (
              <li
                key={song.id}
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  padding: "10px",
                  marginBottom: "10px",
                  borderRadius: "10px",
                  backgroundColor: "#111",
                  border: "1px solid #333"
                }}
              >
                <span>
                  {song.title}
                  {song.artists?.length > 0
                    ? ` - ${song.artists.join(", ")}`
                    : ""}
                </span>

                <button onClick={() => handleAddToPlaylist(song)}>
                  ➕ Add
                </button>
              </li>
            ))}

          </ul>
        )}

      </div>

    </div>
  );
}

export default GenreTab;
