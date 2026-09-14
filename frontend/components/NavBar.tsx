"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";

const NAV_LINKS = [
  { href: "/players", label: "Players" },
  { href: "/squad", label: "My Squad" },
  { href: "/matches", label: "Matches" },
];

export default function NavBar() {
  const { token, logout } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  function handleLogout() {
    logout();
    router.push("/login");
  }

  return (
    <header className="border-b border-pulse/30 bg-pitch-panel">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
        <Link href="/players" className="flex items-center gap-2">
          <span className="h-2 w-2 rounded-full bg-pulse" />
          <span className="font-display text-lg font-bold tracking-wide text-chalk">
            TIKI-TECHA
          </span>
        </Link>

        <nav className="flex items-center gap-6">
          {NAV_LINKS.map((link) => {
            const active = pathname === link.href;
            return (
              <Link
                key={link.href}
                href={link.href}
                className={
                  active
                    ? "font-mono text-sm text-pulse"
                    : "font-mono text-sm text-chalk-dim hover:text-chalk"
                }
              >
                {link.label}
              </Link>
            );
          })}

          {token ? (
            <button
              onClick={handleLogout}
              className="bg-transparent text-card-red border border-card-red px-3 py-1 text-sm font-mono hover:bg-card-red hover:text-pitch transition-colors"
            >
              Logout
            </button>
          ) : (
            <div className="flex items-center gap-4">
              <Link href="/login" className="font-mono text-sm text-chalk-dim hover:text-chalk">
                Login
              </Link>
              <Link href="/register" className="font-mono text-sm text-pulse hover:text-chalk">
                Register
              </Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}