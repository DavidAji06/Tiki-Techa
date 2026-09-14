"use client";

import { useEffect, useState, useCallback } from "react";
import { useAuth } from "@/context/AuthContext";
import { SquadResponse } from "@/types/squad";
import { getPositionLabel } from "@/lib/positions";

export default function SquadPage() {
  const { token } = useAuth();

  const [squadData, setSquadData] = useState<SquadResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [notFound, setNotFound] = useState(false);
  const [creating, setCreating] = useState(false);
  const [sellStatus, setSellStatus] = useState<Record<number, string>>({});
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const [lineupStatus, setLineupStatus] = useState<string | null>(null);
  const [savingLineup, setSavingLineup] = useState(false);

  const fetchSquad = useCallback(async () => {
    if (!token) return;

    try {
      const response = await fetch("http://localhost:8080/api/squads/me", {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (response.status === 404) {
        setNotFound(true);
        return;
      }

      if (!response.ok) {
        throw new Error("Failed to load squad");
      }

      const data: SquadResponse = await response.json();
      setSquadData(data);
      setNotFound(false);

      const currentStarters = new Set(
        data.players.filter((p) => p.starting).map((p) => p.player.id)
      );
      setSelectedIds(currentStarters);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unknown error occurred");
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    if (token === null) return;
    fetchSquad();
  }, [token, fetchSquad]);

  async function handleCreateSquad() {
    if (!token) return;
    setCreating(true);
    try {
      const response = await fetch("http://localhost:8080/api/squads", {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!response.ok) throw new Error("Failed to create squad");
      await fetchSquad();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unknown error occurred");
    } finally {
      setCreating(false);
    }
  }

  async function handleSell(playerId: number) {
    if (!token) return;
    setSellStatus((prev) => ({ ...prev, [playerId]: "Selling..." }));
    try {
      const response = await fetch("http://localhost:8080/api/transfers/sell", {
        method: "POST",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify({ playerId }),
      });
      const message = await response.text();
      if (!response.ok) {
        setSellStatus((prev) => ({ ...prev, [playerId]: message }));
        return;
      }
      setSellStatus((prev) => ({ ...prev, [playerId]: "Sold" }));
      await fetchSquad();
    } catch {
      setSellStatus((prev) => ({ ...prev, [playerId]: "Something went wrong" }));
    }
  }

  function toggleSelected(playerId: number) {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(playerId)) {
        next.delete(playerId);
      } else {
        next.add(playerId);
      }
      return next;
    });
  }

  async function handleSaveLineup() {
    if (!token) return;
    setSavingLineup(true);
    setLineupStatus(null);

    try {
      const response = await fetch("http://localhost:8080/api/squads/lineup", {
        method: "PATCH",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify({ startingPlayerIds: Array.from(selectedIds) }),
      });

      const message = await response.text();

      if (!response.ok) {
        setLineupStatus(message);
        return;
      }

      setLineupStatus("Lineup saved");
      await fetchSquad();
    } catch {
      setLineupStatus("Something went wrong");
    } finally {
      setSavingLineup(false);
    }
  }

  if (loading) return <p className="px-6 py-16 font-mono text-chalk-dim">Loading squad...</p>;
  if (error) return <p className="px-6 py-16 font-mono text-card-red">Error: {error}</p>;

  if (notFound) {
    return (
      <div className="mx-auto max-w-5xl px-6 py-16">
        <h1 className="font-display text-3xl font-bold tracking-wide text-chalk">My Squad</h1>
        <p className="mt-3 font-mono text-sm text-chalk-dim">
          No squad yet. Create one to start building your team.
        </p>
        <button onClick={handleCreateSquad} disabled={creating} className="mt-6">
          {creating ? "Creating..." : "Create Squad"}
        </button>
      </div>
    );
  }

  if (!squadData) return null;

  const { squad, players } = squadData;
  const selectionValid = selectedIds.size === 11;

  return (
    <div className="mx-auto max-w-5xl px-6 py-12">
      <div className="flex items-baseline justify-between">
        <h1 className="font-display text-3xl font-bold tracking-wide text-chalk">My Squad</h1>
        <div className="text-right">
          <div className="font-mono text-2xl text-pulse">£{squad.budgetRemaining}m</div>
          <div className="font-mono text-xs text-chalk-dim">budget remaining</div>
        </div>
      </div>

      <div className="mt-8 flex items-center justify-between">
        <span
          className={
            selectionValid
              ? "font-mono text-sm text-pulse"
              : "font-mono text-sm text-chalk-dim"
          }
        >
          {selectedIds.size} / 11 selected for starting XI
        </span>
        <button onClick={handleSaveLineup} disabled={!selectionValid || savingLineup}>
          {savingLineup ? "Saving..." : "Save Lineup"}
        </button>
      </div>
      {lineupStatus && <p className="mt-2 font-mono text-sm text-chalk-dim">{lineupStatus}</p>}

      <table className="mt-6">
        <thead>
          <tr>
            <th></th>
            <th>Name</th>
            <th>Team</th>
            <th>Position</th>
            <th>Purchase Price</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {players.map((entry) => (
            <tr key={entry.id}>
              <td>
                <input
                  type="checkbox"
                  checked={selectedIds.has(entry.player.id)}
                  onChange={() => toggleSelected(entry.player.id)}
                  className="accent-pulse"
                />
              </td>
              <td className="text-chalk">{entry.player.name}</td>
              <td className="text-chalk-dim">{entry.player.team.name}</td>
              <td>
                <span className="border border-chalk-dim px-1.5 py-0.5 font-mono text-xs text-chalk-dim">
                  {getPositionLabel(entry.player.positionId)}
                </span>
              </td>
              <td className="font-mono text-chalk-dim">£{entry.purchasePrice}m</td>
              <td className="font-mono text-xs text-chalk-dim">
                {entry.starting ? "Starting" : "Bench"}
              </td>
              <td className="text-right">
                <button
                  onClick={() => handleSell(entry.player.id)}
                  className="bg-transparent text-card-red border border-card-red px-3 py-1 text-xs hover:bg-card-red hover:text-pitch transition-colors"
                >
                  Sell
                </button>
                {sellStatus[entry.player.id] && (
                  <span className="ml-2 font-mono text-xs text-chalk-dim">
                    {sellStatus[entry.player.id]}
                  </span>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}