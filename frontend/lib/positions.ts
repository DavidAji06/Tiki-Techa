export const POSITION_LABELS: Record<number, string> = {
  1: "GK",
  2: "DEF",
  3: "MID",
  4: "FWD",
};

export function getPositionLabel(positionId: number): string {
  return POSITION_LABELS[positionId] ?? "Unknown";
}