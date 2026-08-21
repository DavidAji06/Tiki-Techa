import { Player } from "./player";

export interface SquadPlayerEntry {
  id: number;
  player: Player;
  purchasePrice: number;
  starting: boolean;
}

export interface Squad {
  id: number;
  budgetRemaining: number;
  createdAt: string;
}

export interface SquadResponse {
  squad: Squad;
  players: SquadPlayerEntry[];
}