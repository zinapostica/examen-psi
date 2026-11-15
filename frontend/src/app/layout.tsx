import type { Metadata } from "next";
import Link from "next/link";
import "./globals.css";

export const metadata: Metadata = {
  title: "Expense Copilot",
  description: "Upload a receipt, extract fields with vision AI, confirm, and track expenses.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>
        <div className="shell">
          <header className="nav">
            <Link href="/" className="brand">
              Expense <span>Copilot</span>
            </Link>
            <nav className="nav-links">
              <Link href="/">Upload</Link>
              <Link href="/expenses">Expenses</Link>
            </nav>
          </header>
          {children}
        </div>
      </body>
    </html>
  );
}
