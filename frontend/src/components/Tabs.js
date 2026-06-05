// src/components/Tabs.js
import React, { useState } from "react";
import Discover from "./Discover";
import Playlist from "./Playlist";
import GenreTab from "./GenreTab"; // Combined genre + songs component

const Tabs = () => {
  const [activeTab, setActiveTab] = useState("discover");
  const [selectedPlaylist, setSelectedPlaylist] = useState("");

  return (
    <div>
      {/* Tab buttons */}
      <div style={{ display: "flex", gap: "10px", marginBottom: "20px" }}>
        <button
          onClick={() => setActiveTab("discover")}
          style={{
            padding: "10px 20px",
            borderRadius: "5px",
            border: "none",
            backgroundColor: activeTab === "discover" ? "#ed859f" : "#444",
            color: "#fff",
            cursor: "pointer",
          }}
        >
          Discover Songs
        </button>

        <button
          onClick={() => setActiveTab("playlist")}
          style={{
            padding: "10px 20px",
            borderRadius: "5px",
            border: "none",
            backgroundColor: activeTab === "playlist" ? "#ed859f" : "#444",
            color: "#fff",
            cursor: "pointer",
          }}
        >
          Playlists
        </button>

        <button
          onClick={() => setActiveTab("genre")}
          style={{
            padding: "10px 20px",
            borderRadius: "5px",
            border: "none",
            backgroundColor: activeTab === "genre" ? "#ed859f" : "#444",
            color: "#fff",
            cursor: "pointer",
          }}
        >
          Genres
        </button>
      </div>

      {/* Tab content */}
      {activeTab === "discover" && (
        <Discover
          setActiveTab={setActiveTab}
          selectedPlaylist={selectedPlaylist}
        />
      )}

      {activeTab === "playlist" && (
        <Playlist
          selectedPlaylist={selectedPlaylist}
          setSelectedPlaylist={setSelectedPlaylist}
        />
      )}

      {activeTab === "genre" && <GenreTab />} {/* Combined Genre + Songs */}
    </div>
  );
};

export default Tabs;