import { HttpResourceRef } from '@angular/common/http';
import { Observable, of, tap } from 'rxjs';
import { GetApiEndpoint, ApiParam } from './api.service';
import { Page } from '../models';
import { Signal } from '@angular/core';

export abstract class CacheService<T, U = T> {
  private cache: Record<string, HttpResourceRef<T | undefined>> = {};

  protected abstract rawGet(uuid: string): GetApiEndpoint<T>;
  protected abstract rawUpdate(uuid: string, update: U): Observable<T>;
  protected abstract rawDelete(uuid: string, params?: Record<string, ApiParam>): Observable<void>;

  public get(uuid: string): HttpResourceRef<T | undefined> {
    return this.cache[uuid] ??= this.rawGet(uuid).asResource();
  }

  public getObservable(uuid: string): Observable<T> {
    const cached = this.cache[uuid]?.value();
    if(cached !== undefined) return of(cached);
    return this.rawGet(uuid).asObservable();
  }

  public update(uuid: string, update: U): Observable<T> {
    return this.rawUpdate(uuid, update).pipe(
      tap(v => this.postUpdate(uuid, v))
    );
  }

  protected postUpdate(uuid: string, updated: T) {
    this.cache[uuid]?.set(updated);
  }

  public delete(uuid: string, params?: Record<string, ApiParam>): Observable<void> {
    return this.rawDelete(uuid, params).pipe(
      tap(() => {
        this.cache[uuid]?.set(undefined);
        delete this.cache[uuid];
      })
    );
  }

  public reload(uuid: string): void {
    this.cache[uuid]?.reload();
  }

  public clear(uuid?: string): void {
    if(uuid === undefined) this.cache = {};
    else delete this.cache[uuid];
  }
}

export abstract class NamedCacheService<T, U = T> extends CacheService<T, U> {
  public abstract search(
    name?: Signal<string> | string,
    page?: Signal<number> | number,
    size?: Signal<number> | number
  ): HttpResourceRef<Page<T> | undefined>;
}
