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

  return (
    <div>
      <h1>Simulate a Match</h1>
      <form onSubmit={handleSimulate}>
        <div>
          <label htmlFor="homeSquadId">Home Squad ID</label>
          <input
            id="homeSquadId"
            type="number"
            value={homeSquadId}
            onChange={(e) => setHomeSquadId(e.target.value)}
            required
          />
        </div>
        <div>
          <label htmlFor="awaySquadId">Away Squad ID</label>
          <input
            id="awaySquadId"
            type="number"
            value={awaySquadId}
            onChange={(e) => setAwaySquadId(e.target.value)}
            required
          />
        </div>
        <button type="submit" disabled={loading}>
          {loading ? "Simulating..." : "Simulate Match"}
        </button>
      </form>

      {error && <p>Error: {error}</p>}

      {match && (
        <div>
          <h2>
            Squad {match.homeSquad.id} {match.homeScore} - {match.awayScore} Squad {match.awaySquad.id}
          </h2>

          <h3>Match Events</h3>
          {sortedEvents.length === 0 ? (
            <p>No events recorded.</p>
          ) : (
            <ul>
              {sortedEvents.map((event) => (
                <li key={event.id}>
                  {event.minute}&apos; — {event.eventType.replace("_", " ")} — {event.player.name}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}