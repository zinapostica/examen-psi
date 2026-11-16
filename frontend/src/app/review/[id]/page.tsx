"use client";

import { FormEvent, useEffect, useState, useTransition } from "react";
import { useParams, useRouter } from "next/navigation";
import {
  CATEGORIES,
  CreateExpensePayload,
  ExpenseCategory,
  ReceiptUpload,
  createExpense,
  extractReceipt,
  getReceipt,
} from "@/lib/api";

export default function ReviewPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const [receipt, setReceipt] = useState<ReceiptUpload | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();
  const [form, setForm] = useState({
    merchant: "",
    expenseDate: "",
    amount: "",
    currency: "EUR",
    category: "FOOD" as ExpenseCategory,
    notes: "",
    confidence: "",
  });

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        let data = await getReceipt(params.id);
        if (data.status === "UPLOADED" || data.status === "FAILED") {
          data = await extractReceipt(params.id);
        }
        if (cancelled) return;
        setReceipt(data);
        setForm({
          merchant: data.merchant ?? "",
          expenseDate: data.expenseDate ?? new Date().toISOString().slice(0, 10),
          amount: data.amount != null ? String(data.amount) : "",
          currency: data.currency ?? "EUR",
          category: data.category ?? "FOOD",
          notes: "",
          confidence: data.confidence != null ? String(data.confidence) : "",
        });
      } catch (e) {
        if (!cancelled) {
          setError(e instanceof Error ? e.message : "Failed to load receipt");
        }
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [params.id]);

  function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    startTransition(async () => {
      try {
        const payload: CreateExpensePayload = {
          uploadId: params.id,
          merchant: form.merchant.trim(),
          expenseDate: form.expenseDate,
          amount: Number(form.amount),
          currency: form.currency.toUpperCase(),
          category: form.category,
          notes: form.notes.trim() || undefined,
          confidence: form.confidence ? Number(form.confidence) : null,
        };
        await createExpense(payload);
        setToast("Expense saved");
        setTimeout(() => router.push("/expenses"), 700);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Save failed");
      }
    });
  }

  async function reExtract() {
    setError(null);
    startTransition(async () => {
      try {
        // Force by calling extract; backend returns cached EXTRACTED unless we reset — for MVP,
        // if already EXTRACTED the API returns cached draft. Show message accordingly.
        const data = await extractReceipt(params.id);
        setReceipt(data);
        setForm((prev) => ({
          ...prev,
          merchant: data.merchant ?? prev.merchant,
          expenseDate: data.expenseDate ?? prev.expenseDate,
          amount: data.amount != null ? String(data.amount) : prev.amount,
          currency: data.currency ?? prev.currency,
          category: data.category ?? prev.category,
          confidence: data.confidence != null ? String(data.confidence) : prev.confidence,
        }));
        setToast("Draft refreshed");
      } catch (err) {
        setError(err instanceof Error ? err.message : "Re-extract failed");
      }
    });
  }

  if (error && !receipt) {
    return (
      <main style={{ padding: "2rem 0" }}>
        <p role="alert" style={{ color: "var(--danger)", fontWeight: 600 }}>
          {error}
        </p>
        <p style={{ color: "var(--ink-soft)" }}>
          You can still go back and upload again, or enter fields manually after a successful upload.
        </p>
      </main>
    );
  }

  if (!receipt) {
    return (
      <main style={{ padding: "3rem 0", color: "var(--ink-soft)" }}>
        Loading extract preview…
      </main>
    );
  }

  return (
    <main style={{ paddingBottom: "3rem" }}>
      <section className="hero" style={{ paddingBottom: "1rem" }}>
        <p className="badge">Status: {receipt.status}</p>
        <h1 style={{ maxWidth: "18ch", fontSize: "clamp(2rem, 4vw, 2.8rem)" }}>Review extract</h1>
        <p>Edit anything the model got wrong, then save to the ledger.</p>
      </section>

      <div className="panel review-grid">
        <div>
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src={receipt.imageUrl}
            alt="Uploaded receipt"
            style={{
              width: "100%",
              borderRadius: 14,
              border: "1px solid var(--line)",
              background: "#fff",
              minHeight: 220,
              objectFit: "contain",
            }}
          />
          {receipt.rawExtractJson && (
            <details style={{ marginTop: "0.85rem" }}>
              <summary style={{ cursor: "pointer", color: "var(--ink-soft)", fontWeight: 600 }}>
                Raw extract JSON
              </summary>
              <pre
                style={{
                  whiteSpace: "pre-wrap",
                  fontSize: "0.8rem",
                  background: "rgba(28,36,48,0.04)",
                  padding: "0.75rem",
                  borderRadius: 12,
                  overflow: "auto",
                }}
              >
                {receipt.rawExtractJson}
              </pre>
            </details>
          )}
        </div>

        <form onSubmit={onSubmit} style={{ display: "grid", gap: "0.85rem" }}>
          <div className="field">
            <label htmlFor="merchant">Merchant</label>
            <input
              id="merchant"
              required
              value={form.merchant}
              onChange={(e) => setForm({ ...form, merchant: e.target.value })}
            />
          </div>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "0.75rem" }}>
            <div className="field">
              <label htmlFor="expenseDate">Date</label>
              <input
                id="expenseDate"
                type="date"
                required
                value={form.expenseDate}
                onChange={(e) => setForm({ ...form, expenseDate: e.target.value })}
              />
            </div>
            <div className="field">
              <label htmlFor="amount">Amount</label>
              <input
                id="amount"
                type="number"
                min="0.01"
                step="0.01"
                required
                value={form.amount}
                onChange={(e) => setForm({ ...form, amount: e.target.value })}
              />
            </div>
          </div>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "0.75rem" }}>
            <div className="field">
              <label htmlFor="currency">Currency</label>
              <input
                id="currency"
                maxLength={3}
                required
                value={form.currency}
                onChange={(e) => setForm({ ...form, currency: e.target.value.toUpperCase() })}
              />
            </div>
            <div className="field">
              <label htmlFor="category">Category</label>
              <select
                id="category"
                value={form.category}
                onChange={(e) =>
                  setForm({ ...form, category: e.target.value as ExpenseCategory })
                }
              >
                {CATEGORIES.map((c) => (
                  <option key={c} value={c}>
                    {c}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div className="field">
            <label htmlFor="notes">Notes (optional)</label>
            <textarea
              id="notes"
              rows={3}
              value={form.notes}
              onChange={(e) => setForm({ ...form, notes: e.target.value })}
            />
          </div>
          <div className="field">
            <label htmlFor="confidence">Model confidence</label>
            <input id="confidence" value={form.confidence} readOnly />
          </div>

          {error && (
            <p role="alert" style={{ color: "var(--danger)", margin: 0, fontWeight: 600 }}>
              {error}
            </p>
          )}

          <div style={{ display: "flex", gap: "0.75rem", flexWrap: "wrap", marginTop: "0.25rem" }}>
            <button className="btn btn-primary" type="submit" disabled={pending}>
              {pending ? "Saving…" : "Confirm & save"}
            </button>
            <button className="btn btn-ghost" type="button" disabled={pending} onClick={reExtract}>
              Re-extract
            </button>
          </div>
        </form>
      </div>

      {toast && <div className="toast">{toast}</div>}
    </main>
  );
}
