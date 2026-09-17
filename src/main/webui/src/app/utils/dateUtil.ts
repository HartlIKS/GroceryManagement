import { computed, Signal } from '@angular/core';

export function toDate(date: string | undefined): Date | undefined {
  if (date) return new Date(date);
  return undefined;
}

export function toDateSignal(date: Signal<string | undefined>): Signal<Date | undefined> {
  return computed(() => toDate(date()));
}
