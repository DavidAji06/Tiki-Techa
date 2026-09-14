"use client";

import { useState } from "react";
import { Match, MatchEvent } from "@/types/match";

export default function MatchesPage() {
  const [homeSquadId, setHomeSquadId] = useState("");
  const [awaySquadId, setAwaySquadId] = useState("");
  const [match, setMatch] = useState<Match | null>(null);
  const [events, setEvents] = useState<MatchEvent[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSimulate(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    setMatch(null);
    setEvents([]);

    try {
      const matchResponse = await fetch("http://localhost:8080/api/matches", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          homeSquadId: Number(homeSquadId),
          awaySquadId: Number(awaySquadId),
        }),
      });

      if (!matchResponse.ok) {
        const message = await matchResponse.text();
        throw new Error(message || "Failed to simulate match");
      }

      const matchData: Match = await matchResponse.json();
      setMatch(matchData);

      const eventsResponse = await fetch(
        `http://localhost:8080/api/matches/${matchData.id}/events`
      );
      if (eventsResponse.ok) {
        const eventsData: MatchEvent[] = await eventsResponse.json();
        setEvents(eventsData);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  const sortedEvents = [...events].sort((a, b) => a.minute - b.minute);

  const eventDotColor = (type: MatchEvent["eventType"]) => {
    if (type === "GOAL") return "bg-pulse";
    if (type === "RED_CARD") return "bg-card-red";
    return "bg-chalk-dim";
  };

  return (
    <div className="mx-auto max-w-2xl px-6 py-12">
      <h1 className="font-display text-3xl font-bold tracking-wide text-chalk">Simulate a Match</h1>
      <p className="mt-1 font-mono text-sm text-chalk-dim">Enter two squad IDs to run a fixture.</p>

      <form onSubmit={handleSimulate} className="mt-8 flex items-end gap-4">
        <div className="flex flex-col gap-1">
          <label htmlFor="homeSquadId" className="font-mono text-xs uppercase tracking-wider text-chalk-dim">
            Home Squad
          </label>
          <input
            id="homeSquadId"
            type="number"
            value={homeSquadId}
            onChange={(e) => setHomeSquadId(e.target.value)}
            required
            className="w-24"
          />
        </div>

        <span className="pb-2 font-mono text-chalk-dim">vs</span>

        <div className="flex flex-col gap-1">
          <label htmlFor="awaySquadId" className="font-mono text-xs uppercase tracking-wider text-chalk-dim">
            Away Squad
          </label>
          <input
            id="awaySquadId"
            type="number"
            value={awaySquadId}
            onChange={(e) => setAwaySquadId(e.target.value)}
            required
            className="w-24"
          />
        </div>

        <button type="submit" disabled={loading}>
          {loading ? "Simulating..." : "Simulate"}
        </button>
      </form>

      {error && <p className="mt-4 font-mono text-sm text-card-red">{error}</p>}

      {match && (
        <div className="mt-12">
          <div className="flex items-center justify-center gap-6 border border-pulse/30 bg-pitch-panel py-8">
            <span className="font-mono text-sm text-chalk-dim">Squad {match.homeSquad.id}</span>
            <span className="font-display text-4xl font-bold text-chalk">
              {match.homeScore} – {match.awayScore}
            </span>
            <span className="font-mono text-sm text-chalk-dim">Squad {match.awaySquad.id}</span>
          </div>

          <h2 className="mt-8 font-display text-lg font-bold tracking-wide text-chalk">
            Match Events
          </h2>

          {sortedEvents.length === 0 ? (
            <p className="mt-2 font-mono text-sm text-chalk-dim">No events recorded.</p>
          ) : (
            <ul className="mt-4 flex flex-col gap-2">
              {sortedEvents.map((event) => (
                <li key={event.id} className="flex items-center gap-3 font-mono text-sm">
                  <span className="w-10 text-chalk-dim">{event.minute}&apos;</span>
                  <span className={`h-2 w-2 rounded-full ${eventDotColor(event.eventType)}`} />
                  <span className="text-chalk-dim">{event.eventType.replace("_", " ")}</span>
                  <span className="text-chalk">{event.player.name}</span>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}