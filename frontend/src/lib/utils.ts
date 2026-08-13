import { clsx } from "clsx";
import type { ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]): string {
  return twMerge(clsx(inputs));
}

export function toError(error: unknown): Error | null {
  if (error === null || error === undefined) {
    return null;
  }
  if (error instanceof Error) {
    return error;
  }
  if (typeof error === "string" || typeof error === "number" || typeof error === "boolean") {
    return new Error(String(error));
  }
  try {
    return new Error(JSON.stringify(error));
  } catch {
    return new Error("Unknown error");
  }
}
