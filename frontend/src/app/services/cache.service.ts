import { HttpResourceRef } from '@angular/common/http';
import { Observable, tap } from 'rxjs';


export abstract class CacheService<T, U = T> {
  private cache: Record<string, HttpResourceRef<T | undefined>> = {};

  protected abstract rawGet(uuid: string): HttpResourceRef<T | undefined>;
  protected abstract rawUpdate(uuid: string, update: U): Observable<T>;
  protected abstract rawDelete(uuid: string): Observable<void>;

  public get(uuid: string): HttpResourceRef<T | undefined> {
    return this.cache[uuid] ??= this.rawGet(uuid);
  }

  public update(uuid: string, update: U): Observable<T> {
    return this.rawUpdate(uuid, update).pipe(
      tap(v => this.cache[uuid]?.set(v))
    );
  }

  public delete(uuid: string): Observable<void> {
    return this.rawDelete(uuid).pipe(
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
