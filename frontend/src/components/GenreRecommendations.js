import React, { useEffect, useState } from "react";





 /**
  * Temporary hardcoded username used to simulate an authenticated user during development.
  *
  * This enables testing of user-specific recommendation features. In a production application, this would be replaced with actual authentication logic to determine the logged-in user.
  */


function GenreRecommendations() {

  const [songs, setSongs] = useState([]);
  const [topGenre, setTopGenre] = useState("");
  const [loading, setLoading] = useState(false);

  // TEMP USERNAME
  const username = "sanjay";

  // =========================
  // FETCH RECOMMENDATIONS
  // =========================
  const fetchRecommendations = () => {

    setLoading(true);

    fetch(
      `http://localhost:8080/recommendations/genre?username=${username}`
    )
      .then((res) => res.json())
      .then((data) => {

        console.log(" Genre recommendations:", data);

        setTopGenre(data.topGenre || "");
        setSongs(data.songs || []);
      })
      .catch((err) =>
        console.error(" Failed to fetch recommendations:", err)
      )
      .finally(() => setLoading(false));
  };

  // =========================
  // LOAD ON PAGE START
  // =========================
  useEffect(() => {
    fetchRecommendations();
  }, []);

  // =========================
  // EMPTY STATE
  // =========================
  if (!loading && !topGenre) {

    return (
      <div style={{ marginTop: "30px" }}>

        <h2>Recommended For You</h2>

        <p>
          Like songs from genres to get personalized recommendations.
        </p>

      </div>
    );
  }

  return (
    <div style={{ marginTop: "30px" }}>

      <h2>Recommended For You</h2>

      {topGenre && (
        <p>
          Because you liked <b>{topGenre}</b> music
        </p>
      )}

      {loading ? (

        <p>Loading recommendations...</p>

      ) : songs.length === 0 ? (

        <p>No recommendations available.</p>

      ) : (

        <ul
          style={{
            listStyle: "none",
            padding: 0,
          }}
        >

          {songs.map((song) => (

            <li
              key={song.id}
              style={{
                padding: "8px 0",
                borderBottom: "1px solid #eee",
              }}
            >

              <span>

                {song.title}

                {song.artists?.length > 0
                  ? ` - ${song.artists.join(", ")}`
                  : ""}

              </span>

            </li>

          ))}

        </ul>

      )}

    </div>
  );
}

export default GenreRecommendations;
