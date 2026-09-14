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

      setBuyStatus((prev) => ({ ...prev, [playerId]: "Bought" }));
    } catch {
      setBuyStatus((prev) => ({ ...prev, [playerId]: "Something went wrong" }));
    }
  }

  if (loading) return <p className="px-6 py-16 font-mono text-chalk-dim">Loading players...</p>;
  if (error) return <p className="px-6 py-16 font-mono text-card-red">Error: {error}</p>;

  const totalPages = Math.ceil(players.length / PLAYERS_PER_PAGE);
  const startIndex = (currentPage - 1) * PLAYERS_PER_PAGE;
  const currentPlayers = players.slice(startIndex, startIndex + PLAYERS_PER_PAGE);

  return (
    <div className="mx-auto max-w-5xl px-6 py-12">
      <h1 className="font-display text-3xl font-bold tracking-wide text-chalk">Players</h1>
      <p className="mt-1 font-mono text-sm text-chalk-dim">{players.length} available</p>

      <table className="mt-6">
        <thead>
          <tr>
            <th>Name</th>
            <th>Team</th>
            <th>Position</th>
            <th>Cost</th>
            <th>Points</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {currentPlayers.map((player) => (
            <tr key={player.id}>
              <td className="text-chalk">{player.name}</td>
              <td className="text-chalk-dim">{player.team.name}</td>
              <td>
                <span className="border border-chalk-dim px-1.5 py-0.5 font-mono text-xs text-chalk-dim">
                  {getPositionLabel(player.positionId)}
                </span>
              </td>
              <td className="font-mono text-pulse">£{player.nowCost}m</td>
              <td className="font-mono text-chalk-dim">{player.totalPoints}</td>
              <td className="text-right">
                <button onClick={() => handleBuy(player.id)} className="text-xs">
                  Buy
                </button>
                {buyStatus[player.id] && (
                  <span className="ml-2 font-mono text-xs text-chalk-dim">{buyStatus[player.id]}</span>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="mt-6 flex items-center gap-4">
        <button
          onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
          disabled={currentPage === 1}
        >
          Prev
        </button>
        <span className="font-mono text-sm text-chalk-dim">
          Page {currentPage} / {totalPages}
        </span>
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