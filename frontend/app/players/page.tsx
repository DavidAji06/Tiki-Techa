"use client";

import { useEffect, useState } from "react";
import { Player } from "@/types/player";
import { getPositionLabel } from "@/lib/positions";
import { useAuth } from "@/context/AuthContext";

const PLAYERS_PER_PAGE = 20;

export default function PlayersPage() {
  const { token } = useAuth();
  const [players, setPlayers] = useState<Player[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [buyStatus, setBuyStatus] = useState<Record<number, string>>({});

  useEffect(() => {
    async function fetchPlayers() {
      try {
        const response = await fetch("http://localhost:8080/api/players");
        if (!response.ok) {
          throw new Error(`Backend responded with status ${response.status}`);
        }
        const data: Player[] = await response.json();
        setPlayers(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Unknown error occurred");
      } finally {
        setLoading(false);
      }
    }

    fetchPlayers();
  }, []);

  async function handleBuy(playerId: number) {
    if (!token) {
      setBuyStatus((prev) => ({ ...prev, [playerId]: "Log in to buy players" }));
      return;
    }

    setBuyStatus((prev) => ({ ...prev, [playerId]: "Buying..." }));

    try {
      const response = await fetch("http://localhost:8080/api/transfers/buy", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ playerId, isStarting: false }),
      });

      const message = await response.text();

      if (!response.ok) {
        setBuyStatus((prev) => ({ ...prev, [playerId]: message }));
        return;
      }

      setBuyStatus((prev) => ({ ...prev, [playerId]: "Bought!" }));
    } catch {
      setBuyStatus((prev) => ({ ...prev, [playerId]: "Something went wrong" }));
    }
  }

  if (loading) return <p>Loading players...</p>;
  if (error) return <p>Error: {error}</p>;

  const totalPages = Math.ceil(players.length / PLAYERS_PER_PAGE);
  const startIndex = (currentPage - 1) * PLAYERS_PER_PAGE;
  const currentPlayers = players.slice(startIndex, startIndex + PLAYERS_PER_PAGE);

  return (
    <div>
      <h1>Players</h1>
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Team</th>
            <th>Position</th>
            <th>Cost</th>
            <th>Points</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {currentPlayers.map((player) => (
            <tr key={player.id}>
              <td>{player.name}</td>
              <td>{player.team.name}</td>
              <td>{getPositionLabel(player.positionId)}</td>
              <td>£{player.nowCost}m</td>
              <td>{player.totalPoints}</td>
              <td>
                <button onClick={() => handleBuy(player.id)}>Buy</button>
                {buyStatus[player.id] && <span> {buyStatus[player.id]}</span>}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div>
        <button
          onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
          disabled={currentPage === 1}
        >
          Previous
        </button>
        <span> Page {currentPage} of {totalPages} </span>
        <button
          onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
          disabled={currentPage === totalPages}
        >
          Next
        </button>
      </div>
    </div>
  );
}