export interface Match {
  id: number;
  homeScore: number;
  awayScore: number;
  playedAt: string;
  homeSquad: { id: number };
  awaySquad: { id: number };
}

export interface MatchEvent {
  id: number;
  minute: number;
  eventType: "GOAL" | "YELLOW_CARD" | "RED_CARD";
  player: {
    id: number;
    name: string;
  };
}