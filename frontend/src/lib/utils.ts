import { type ClassValue, clsx } from "clsx";
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
  return new Error(String(error));
}
