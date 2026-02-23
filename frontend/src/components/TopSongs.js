import React from "react";

const TopSongs = () => {
  const topSongs = [
    { id: "1", title: "Shape of You", artists: ["Ed Sheeran"] },
    { id: "2", title: "Blinding Lights", artists: ["The Weeknd"] },
    { id: "3", title: "Levitating", artists: ["Dua Lipa"] },
  ];

  return (
    <div>
      <h2>Top Songs</h2>
      <ul>
        {topSongs.map((song) => (
          <li key={song.id}>
            {song.title} — {song.artists.join(", ")}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default TopSongs;

