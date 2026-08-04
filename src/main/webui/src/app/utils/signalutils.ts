import { computed, isSignal, Signal } from '@angular/core';

export function resolve<T>(value: Signal<T> | T): Signal<T> {
  return isSignal(value) ? value : computed(() => value);
}

export function toDate(date: Signal<string | undefined>): Signal<Date | undefined> {
  return computed(() => {
    const d = date();
    if(d) return new Date(d);
    return undefined;
  });
}
