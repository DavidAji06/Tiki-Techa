export interface Player {
  id: number;
  name: string;
  team: {
    id: number;
    name: string;
    shortName: string;
  };
  positionId: number;
  totalPoints: number;
  nowCost: number;
  selectedByPercent: number;
  minutes: number;
  goalsScored: number;
  assists: number;
}