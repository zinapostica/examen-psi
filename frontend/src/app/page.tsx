"use client";

import { useRouter } from "next/navigation";
import { useCallback, useRef, useState, useTransition } from "react";
import { compressImage } from "@/lib/compressImage";
import { extractReceipt, uploadReceipt } from "@/lib/api";

export default function UploadPage() {
  const router = useRouter();
  const inputRef = useRef<HTMLInputElement>(null);
  const [dragging, setDragging] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [status, setStatus] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();

  const processFile = useCallback(
    (file: File) => {
      setError(null);
      startTransition(async () => {
        try {
          setStatus("Compressing image…");
          const compressed = await compressImage(file);
          setStatus("Uploading receipt…");
          const uploaded = await uploadReceipt(compressed);
          setStatus("Extracting fields…");
          await extractReceipt(uploaded.id);
          router.push(`/review/${uploaded.id}`);
        } catch (e) {
          setStatus(null);
          setError(e instanceof Error ? e.message : "Upload failed");
        }
      });
    },
    [router]
  );

  return (
    <main>
      <section className="hero">
        <p className="badge">Vision → confirm → ledger</p>
        <h1>Expense Copilot</h1>
        <p>
          Turn a phone photo of a receipt into a confirmed expense. Review the AI extract, fix
          anything off, then filter by month or category.
        </p>
        <div style={{ display: "flex", gap: "0.75rem", flexWrap: "wrap" }}>
          <button
            className="btn btn-primary"
            type="button"
            disabled={pending}
            onClick={() => inputRef.current?.click()}
          >
            {pending ? (
              <>
                <span className="spinner" /> &nbsp; Working…
              </>
            ) : (
              "Upload receipt"
            )}
          </button>
          <button
            className="btn btn-ghost"
            type="button"
            disabled={pending}
            onClick={() => {
              if (inputRef.current) {
                inputRef.current.setAttribute("capture", "environment");
                inputRef.current.click();
                inputRef.current.removeAttribute("capture");
              }
            }}
          >
            Use camera
          </button>
        </div>
        {status && (
          <p style={{ color: "var(--accent-deep)", fontWeight: 600, margin: 0 }}>{status}</p>
        )}
        {error && (
          <p style={{ color: "var(--danger)", fontWeight: 600, margin: 0 }} role="alert">
            {error}
          </p>
        )}
      </section>

      <section
        className={`panel dropzone ${dragging ? "active" : ""}`}
        onDragOver={(e) => {
          e.preventDefault();
          setDragging(true);
        }}
        onDragLeave={() => setDragging(false)}
        onDrop={(e) => {
          e.preventDefault();
          setDragging(false);
          const file = e.dataTransfer.files?.[0];
          if (file) processFile(file);
        }}
        style={{ marginBottom: "2.5rem", animation: "rise 0.8s ease 0.1s both" }}
      >
        <p style={{ margin: "0 0 0.5rem", fontFamily: "var(--font-display)", fontSize: "1.35rem" }}>
          Drop a clear receipt photo
        </p>
        <p style={{ margin: 0, color: "var(--ink-soft)" }}>
          JPEG, PNG, or WebP · max 5 MB · client resize before upload · mock extract works offline
        </p>
        <input
          ref={inputRef}
          type="file"
          accept="image/jpeg,image/png,image/webp"
          hidden
          onChange={(e) => {
            const file = e.target.files?.[0];
            if (file) processFile(file);
            e.target.value = "";
          }}
        />
      </section>
    </main>
  );
}
