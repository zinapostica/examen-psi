"use client";

import { useEffect, useMemo, useState, useTransition } from "react";
import {
  CATEGORIES,
  Expense,
  ExpenseCategory,
  listExpenses,
} from "@/lib/api";

function currentMonth() {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

export default function ExpensesPage() {
  const [month, setMonth] = useState(currentMonth());
  const [category, setCategory] = useState<ExpenseCategory | "">("");
  const [items, setItems] = useState<Expense[]>([]);
  const [selected, setSelected] = useState<Expense | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();

  useEffect(() => {
    startTransition(async () => {
      try {
        setError(null);
        const data = await listExpenses({ month, category });
        setItems(data);
      } catch (e) {
        setError(e instanceof Error ? e.message : "Failed to load expenses");
      }
    });
  }, [month, category]);

  // Sum per currency — do not mix RON/EUR into a fake € total (no FX in MVP).
  const totalsByCurrency = useMemo(() => {
    const map = new Map<string, number>();
    for (const e of items) {
      const code = (e.currency || "EUR").toUpperCase();
      map.set(code, (map.get(code) ?? 0) + Number(e.amount));
    }
    return [...map.entries()].sort(([a], [b]) => a.localeCompare(b));
  }, [items]);

  const totalsLabel = useMemo(() => {
    if (totalsByCurrency.length === 0) return "0.00";
    return totalsByCurrency
      .map(([code, sum]) => `${sum.toFixed(2)} ${code}`)
      .join(" · ");
  }, [totalsByCurrency]);

  return (
    <main style={{ paddingBottom: "3rem" }}>
      <section className="hero" style={{ paddingBottom: "1rem" }}>
        <p className="badge">Ledger</p>
        <h1 style={{ maxWidth: "12ch", fontSize: "clamp(2rem, 4vw, 2.8rem)" }}>Expenses</h1>
        <p>Filter by month and category. Click a row for receipt detail.</p>
      </section>

      <section
        className="panel"
        style={{ padding: "1.1rem", marginBottom: "1rem", animation: "rise 0.5s ease both" }}
      >
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))",
            gap: "0.85rem",
            alignItems: "end",
          }}
        >
          <div className="field">
            <label htmlFor="month">Month</label>
            <input
              id="month"
              type="month"
              value={month}
              onChange={(e) => setMonth(e.target.value)}
            />
          </div>
          <div className="field">
            <label htmlFor="category">Category</label>
            <select
              id="category"
              value={category}
              onChange={(e) => setCategory(e.target.value as ExpenseCategory | "")}
            >
              <option value="">All</option>
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </div>
          <div style={{ color: "var(--ink-soft)", fontWeight: 600 }}>
            {pending
              ? "Loading…"
              : `${items.length} expense${items.length === 1 ? "" : "s"} · ${totalsLabel}`}
          </div>
        </div>
      </section>

      {error && (
        <p role="alert" style={{ color: "var(--danger)", fontWeight: 600 }}>
          {error}
        </p>
      )}

      <section className="panel" style={{ padding: "0.5rem 1rem 1rem", overflowX: "auto" }}>
        <table className="table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Merchant</th>
              <th>Category</th>
              <th>Amount</th>
            </tr>
          </thead>
          <tbody>
            {items.length === 0 && !pending ? (
              <tr>
                <td colSpan={4} style={{ color: "var(--ink-soft)", cursor: "default" }}>
                  No expenses for this filter. Upload a receipt or clear filters.
                </td>
              </tr>
            ) : (
              items.map((expense) => (
                <tr key={expense.id} onClick={() => setSelected(expense)}>
                  <td>{expense.expenseDate}</td>
                  <td style={{ fontWeight: 600 }}>{expense.merchant}</td>
                  <td>
                    <span className="badge">{expense.category}</span>
                  </td>
                  <td>
                    {Number(expense.amount).toFixed(2)} {expense.currency}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </section>

      {selected && (
        <div
          role="dialog"
          aria-modal="true"
          onClick={() => setSelected(null)}
          style={{
            position: "fixed",
            inset: 0,
            background: "rgba(28,36,48,0.45)",
            display: "grid",
            placeItems: "center",
            padding: "1rem",
            zIndex: 40,
          }}
        >
          <div
            className="panel"
            onClick={(e) => e.stopPropagation()}
            style={{
              width: "min(640px, 100%)",
              padding: "1.25rem",
              display: "grid",
              gap: "0.85rem",
              animation: "rise 0.3s ease both",
            }}
          >
            <div style={{ display: "flex", justifyContent: "space-between", gap: "1rem" }}>
              <div>
                <h2
                  style={{
                    margin: 0,
                    fontFamily: "var(--font-display)",
                    fontSize: "1.6rem",
                  }}
                >
                  {selected.merchant}
                </h2>
                <p style={{ margin: "0.35rem 0 0", color: "var(--ink-soft)" }}>
                  {selected.expenseDate} · {selected.category}
                </p>
              </div>
              <button className="btn btn-ghost" type="button" onClick={() => setSelected(null)}>
                Close
              </button>
            </div>
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src={selected.imageUrl}
              alt={`Receipt for ${selected.merchant}`}
              style={{
                width: "100%",
                maxHeight: 280,
                objectFit: "contain",
                borderRadius: 12,
                border: "1px solid var(--line)",
                background: "#fff",
              }}
            />
            <p style={{ margin: 0, fontSize: "1.2rem", fontWeight: 700 }}>
              {Number(selected.amount).toFixed(2)} {selected.currency}
            </p>
            {selected.notes && (
              <p style={{ margin: 0, color: "var(--ink-soft)" }}>{selected.notes}</p>
            )}
          </div>
        </div>
      )}
    </main>
  );
}
