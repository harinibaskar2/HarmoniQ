import React, { useState, useEffect, useContext } from "react";
import { PlaylistContext } from "../context/PlaylistContext";

function GenreTab() {

  const [genres, setGenres] = useState([]);
  const [selectedGenre, setSelectedGenre] = useState("");
  const [songs, setSongs] = useState([]);
  const [playlistName, setPlaylistName] = useState("");
  const [loading, setLoading] = useState(false);

  const { addSongToPlaylist } = useContext(PlaylistContext);

  // TEMP USERNAME
  // make sure this matches Playlist.js
  const username = "sanjay";

  // =========================
  // FETCH GENRES
  // =========================
  useEffect(() => {

    console.log("🔥 Fetching genres...");

    fetch("http://localhost:8080/genres")
      .then((res) => res.json())
      .then((data) => {

        console.log("✅ Genres loaded:", data);

        setGenres(data);
      })
      .catch((err) =>
        console.error("❌ Failed to fetch genres:", err)
      );

  }, []);

  // =========================
  // FETCH SONGS BY GENRE
  // =========================
  const fetchSongsByGenre = () => {

    if (!selectedGenre) return;

    console.log("🔥 Fetching songs for genre:", selectedGenre);

    setLoading(true);

    fetch(
      `http://localhost:8080/songs/genre?genre=${encodeURIComponent(
        selectedGenre
      )}`
    )
      .then((res) => res.json())
      .then((data) => {

        console.log("✅ Songs loaded:", data);

        setSongs(data);
      })
      .catch((err) =>
        console.error("❌ Failed to fetch songs:", err)
      )
      .finally(() => setLoading(false));
  };

  // =========================
  // SEND FEEDBACK
  // =========================
  const sendFeedback = async (song, type) => {

    try {

      console.log("🔥 SENDING FEEDBACK");
      console.log("USERNAME =", username);
      console.log("GENRE =", selectedGenre);
      console.log("SONG =", song.title);
      console.log("TYPE =", type);

      const response = await fetch(
        "http://localhost:8080/feedback/genre-song",
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json",
          },

          body: JSON.stringify({
            username: username,
            genre: selectedGenre,
            songId: song.id,
            type: type,
          }),
        }
      );

      const data = await response.json();

      console.log("✅ FEEDBACK RESPONSE:", data);

    } catch (err) {

      console.error("❌ Feedback error:", err);
    }
  };

  // =========================
  // HANDLE GENRE CHANGE
  // =========================
  const handleChange = (e) => {

    console.log("🎧 Selected genre:", e.target.value);

    setSelectedGenre(e.target.value);

    setSongs([]);
  };

  // =========================
  // ADD TO PLAYLIST
  // =========================
  const handleAddToPlaylist = (song) => {

    console.log("➕ Adding to playlist:", song.title);

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

      {/* ================= GENRE DROPDOWN ================= */}
      <div style={{ marginBottom: "10px" }}>

        <select
          value={selectedGenre}
          onChange={handleChange}
        >

          <option value="">
            Select a genre
          </option>

          {genres.map((g) => (
            <option
              key={g}
              value={g}
            >
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

      {/* ================= PLAYLIST INPUT ================= */}
      <input
        type="text"
        placeholder="Playlist name (optional)"
        value={playlistName}
        onChange={(e) => setPlaylistName(e.target.value)}
        style={{
          marginBottom: "20px",
          width: "200px",
        }}
      />

      {/* ================= EMPTY STATE ================= */}
      {!selectedGenre && (
        <p>Please select a genre to see songs.</p>
      )}

      {/* ================= SONGS ================= */}
      {selectedGenre && (

        <div>

          <h3>
            Songs in "{selectedGenre}"
          </h3>

          {loading ? (

            <p>Loading songs...</p>

          ) : songs.length === 0 ? (

            <p>No songs found for this genre.</p>

          ) : (

            <ul
              style={{
                padding: 0,
                listStyle: "none",
              }}
            >

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

                  {/* SONG INFO */}
                  <span>

                    {song.title}

                    {song.artists?.length > 0
                      ? ` - ${song.artists.join(", ")}`
                      : ""}

                  </span>

                  {/* ACTIONS */}
                  <div
                    style={{
                      display: "flex",
                      gap: "8px",
                    }}
                  >

                    {/* LIKE */}
                    <button
                      onClick={() =>
                        sendFeedback(song, "like")
                      }
                    >
                      👍
                    </button>

                    {/* DISLIKE */}
                    <button
                      onClick={() =>
                        sendFeedback(song, "dislike")
                      }
                    >
                      👎
                    </button>

                    {/* ADD */}
                    <button
                      onClick={() =>
                        handleAddToPlaylist(song)
                      }
                    >
                      Add
                    </button>

                  </div>

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