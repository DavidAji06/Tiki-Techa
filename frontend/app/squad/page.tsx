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

      // initialize checkbox selection from the currently saved starting XI
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
      setSellStatus((prev) => ({ ...prev, [playerId]: "Sold!" }));
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

      setLineupStatus("Lineup saved!");
      await fetchSquad();
    } catch {
      setLineupStatus("Something went wrong");
    } finally {
      setSavingLineup(false);
    }
  }

  if (loading) return <p>Loading squad...</p>;
  if (error) return <p>Error: {error}</p>;

  if (notFound) {
    return (
      <div>
        <p>You don&apos;t have a squad yet.</p>
        <button onClick={handleCreateSquad} disabled={creating}>
          {creating ? "Creating..." : "Create Squad"}
        </button>
      </div>
    );
  }

  if (!squadData) return null;

  const { squad, players } = squadData;

  return (
    <div>
      <h1>My Squad</h1>
      <p>Budget remaining: £{squad.budgetRemaining}m</p>
      <p>{selectedIds.size}/11 selected for starting XI</p>

      <table>
        <thead>
          <tr>
            <th>Starting</th>
            <th>Name</th>
            <th>Team</th>
            <th>Position</th>
            <th>Purchase Price</th>
            <th>Status</th>
            <th>Action</th>
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
                />
              </td>
              <td>{entry.player.name}</td>
              <td>{entry.player.team.name}</td>
              <td>{getPositionLabel(entry.player.positionId)}</td>
              <td>£{entry.purchasePrice}m</td>
              <td>{entry.starting ? "Starting" : "Bench"}</td>
              <td>
                <button onClick={() => handleSell(entry.player.id)}>Sell</button>
                {sellStatus[entry.player.id] && <span> {sellStatus[entry.player.id]}</span>}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <button onClick={handleSaveLineup} disabled={selectedIds.size !== 11 || savingLineup}>
        {savingLineup ? "Saving..." : "Save Lineup"}
      </button>
      {lineupStatus && <p>{lineupStatus}</p>}
    </div>
  );
}