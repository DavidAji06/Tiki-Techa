"use client";

import { useEffect, useState } from "react";

interface HealthResponse {
  status: string;
  database: string;
  message: string;
  error?: string;
}

export default function Home() {
  const [data, setData] = useState<HealthResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    fetch("http://localhost:8080/api/health")
      .then((res) => res.json())
      .then((resData: HealthResponse) => {
        setData(resData);
        setLoading(false);
      })
      .catch((err) => {
        setData({
          status: "DOWN",
          database: "DISCONNECTED",
          message: "Failed to connect to backend",
          error: err.message,
        });
        setLoading(false);
      });
  }, []);

  return (
    <main className="min-h-screen bg-slate-900 text-white p-12 font-sans flex flex-col items-center justify-center">
      <div className="max-w-md w-full bg-slate-800 border border-slate-700 rounded-xl p-6 shadow-xl">
        <h1 className="text-2xl font-bold mb-4 text-emerald-400">
          Tiki-Techa Pipe Check
        </h1>

        {loading ? (
          <p className="text-slate-400 animate-pulse">Testing system connections...</p>
        ) : (
          <div className="space-y-4">
            <div className="flex justify-between items-center border-b border-slate-700 pb-2">
              <span className="text-slate-400">Backend API:</span>
              <span
                className={`font-mono font-bold ${
                  data?.status === "UP" ? "text-emerald-400" : "text-rose-500"
                }`}
              >
                {data?.status}
              </span>
            </div>

            <div className="flex justify-between items-center border-b border-slate-700 pb-2">
              <span className="text-slate-400">PostgreSQL DB:</span>
              <span
                className={`font-mono font-bold ${
                  data?.database === "CONNECTED"
                    ? "text-emerald-400"
                    : "text-rose-500"
                }`}
              >
                {data?.database}
              </span>
            </div>

            <div className="bg-slate-900 p-3 rounded border border-slate-700 mt-4">
              <p className="text-xs text-slate-400 mb-1 font-semibold">Message:</p>
              <p className="text-sm font-mono text-slate-200">
                {data?.message || data?.error}
              </p>
            </div>
          </div>
        )}
      </div>
    </main>
  );
}