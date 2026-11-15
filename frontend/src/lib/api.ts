const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export type ExpenseCategory =
  | "FOOD"
  | "TRANSPORT"
  | "OFFICE"
  | "TRAVEL"
  | "UTILITIES"
  | "OTHER";

export type UploadStatus =
  | "UPLOADED"
  | "EXTRACTING"
  | "EXTRACTED"
  | "FAILED"
  | "CONFIRMED";

export interface ReceiptUpload {
  id: string;
  status: UploadStatus;
  contentType: string;
  sizeBytes: number;
  imageUrl: string;
  merchant: string | null;
  expenseDate: string | null;
  amount: number | null;
  currency: string | null;
  category: ExpenseCategory | null;
  confidence: number | null;
  rawExtractJson: string | null;
  createdAt: string;
}

export interface Expense {
  id: string;
  uploadId: string;
  merchant: string;
  expenseDate: string;
  amount: number;
  currency: string;
  category: ExpenseCategory;
  notes: string | null;
  confidence: number | null;
  imageUrl: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateExpensePayload {
  uploadId: string;
  merchant: string;
  expenseDate: string;
  amount: number;
  currency: string;
  category: ExpenseCategory;
  notes?: string;
  confidence?: number | null;
}

async function parseJson<T>(res: Response): Promise<T> {
  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) {
    const message = data?.message || data?.error || `Request failed (${res.status})`;
    const err = new Error(message) as Error & { fieldErrors?: Record<string, string> };
    err.fieldErrors = data?.fieldErrors;
    throw err;
  }
  return data as T;
}

export async function uploadReceipt(file: File): Promise<ReceiptUpload> {
  const form = new FormData();
  form.append("file", file);
  const res = await fetch(`${API_BASE}/api/receipts`, {
    method: "POST",
    body: form,
  });
  return parseJson(res);
}

export async function extractReceipt(id: string): Promise<ReceiptUpload> {
  const res = await fetch(`${API_BASE}/api/receipts/${id}/extract`, {
    method: "POST",
  });
  return parseJson(res);
}

export async function getReceipt(id: string): Promise<ReceiptUpload> {
  const res = await fetch(`${API_BASE}/api/receipts/${id}`);
  return parseJson(res);
}

export async function createExpense(payload: CreateExpensePayload): Promise<Expense> {
  const res = await fetch(`${API_BASE}/api/expenses`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  return parseJson(res);
}

export async function listExpenses(params: {
  month?: string;
  category?: ExpenseCategory | "";
}): Promise<Expense[]> {
  const qs = new URLSearchParams();
  if (params.month) qs.set("month", params.month);
  if (params.category) qs.set("category", params.category);
  const res = await fetch(`${API_BASE}/api/expenses?${qs.toString()}`, {
    cache: "no-store",
  });
  return parseJson(res);
}

export async function getExpense(id: string): Promise<Expense> {
  const res = await fetch(`${API_BASE}/api/expenses/${id}`, { cache: "no-store" });
  return parseJson(res);
}

export const CATEGORIES: ExpenseCategory[] = [
  "FOOD",
  "TRANSPORT",
  "OFFICE",
  "TRAVEL",
  "UTILITIES",
  "OTHER",
];

export { API_BASE };
