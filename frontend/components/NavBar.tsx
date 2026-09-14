"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";

export default function NavBar() {
  const { token, logout } = useAuth();
  const router = useRouter();

  function handleLogout() {
    logout();
    router.push("/login");
  }

  return (
    <nav>
      <Link href="/players">Players</Link>
      {" | "}
      <Link href="/squad">My Squad</Link>
      {" | "}
      <Link href="/matches">Matches</Link>
      {" | "}
      {token ? (
        <button onClick={handleLogout}>Logout</button>
      ) : (
        <>
          <Link href="/login">Login</Link>
          {" | "}
          <Link href="/register">Register</Link>
        </>
      )}
    </nav>
  );
}