import { computed, inject, Injectable, Injector, isSignal, signal, Signal } from '@angular/core';
import { HttpClient, HttpParams, httpResource, HttpResourceRef } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { resolve } from '../utils/signalutils';
import { AuthService } from './auth.service';

export type ApiParam = string | Date | number | boolean | string[] | undefined;
export interface GetApiEndpoint<T> {
  asResource(): HttpResourceRef<T | undefined>;
  asObservable(): Observable<T>;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly injector = inject(Injector);
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly accessToken = inject(AuthService).accessToken;
  private readonly headers = computed((): Record<string, string> => {
    const token = this.accessToken();
    if(token !== undefined) return {Authorization: `Bearer ${token}`};
    return {};
  });
  public readonly share = signal<string | undefined>(undefined);
  private readonly shareParams = computed(() => {
    const s = this.share();
    if(s === undefined) return undefined;
    return {share: s};
  })

  private toParamSignal(params: Record<string, Signal<ApiParam> | ApiParam> = {}): Signal<HttpParams> {
    const resolvedParams: Record<string, Signal<ApiParam>> = Object.fromEntries(
      Object.entries(params ?? {})
        .map(([k, v]) => [k, resolve(v)])
    );
    return computed(() => {
      let httpParams = new HttpParams();
      for (const [key, valueSignal] of Object.entries(resolvedParams)) {
        if(key === 'share') {
          const shareId = this.share();
          if(shareId !== undefined) httpParams = httpParams.set('share', shareId);
        }
        let value = valueSignal();
        if (value !== null && value !== undefined) {
          if (Array.isArray(value)) {
            for (const item of value) httpParams = httpParams.append(key, String(item));
          } else if (value instanceof Date) {
            httpParams = httpParams.set(key, value.toISOString());
          } else {
            httpParams = httpParams.set(key, String(value));
          }
        }
      }
      this.accessToken();
      return httpParams;
    });
  }

  // Generic CRUD operations
  get<T>(endpoint: string, params?: Record<string, Signal<ApiParam> | ApiParam>) {
    const httpParams = this.toParamSignal(params);

    return httpResource<T>(() => ({
      url: `${this.baseUrl}${endpoint}`,
      params: httpParams(),
      headers: this.headers(),
      credentials: 'include',
    }), {
      injector: this.injector,
    });
  }

  getById<T>(endpoint: string, id: Signal<string | undefined>): HttpResourceRef<T | undefined>;
  getById<T>(endpoint: string, id: string): GetApiEndpoint<T>;
  getById<T>(endpoint: string, id: Signal<string | undefined> | string): HttpResourceRef<T | undefined> | GetApiEndpoint<T> {
    if(isSignal(id)) return httpResource<T>(() => {
      const uuid = id();
      if(uuid === undefined) return undefined;
      return {
        url: `${this.baseUrl}${endpoint}/${uuid}`,
        headers: this.headers(),
        params: this.shareParams(),
        credentials: 'include',
      };
    }, {
      injector: this.injector,
    });
    return {
      asResource: (): HttpResourceRef<T | undefined> => this.getById<T>(endpoint, computed(() => id)),
      asObservable: (): Observable<T> => this.http.get<T>(`${this.baseUrl}${endpoint}/${id}`, {
        headers: this.headers(),
        params: this.shareParams(),
        withCredentials: true,
      }),
    };
  }

  post<T>(endpoint: string, data: any): Observable<T> {
    return this.http.post<T>(`${this.baseUrl}${endpoint}`, data, {
      headers: this.headers(),
      params: this.shareParams(),
      withCredentials: true,
    });
  }

  put<T>(endpoint: string, id: string, data: any): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}${endpoint}/${id}`, data, {
      headers: this.headers(),
      params: this.shareParams(),
      withCredentials: true,
    });
  }

  delete(endpoint: string, id: string, params?: Record<string, ApiParam>): Observable<void> {
    const httpParams = this.toParamSignal(params);
    return this.http.delete<void>(`${this.baseUrl}${endpoint}/${id}`, {
      params: httpParams(),
      headers: this.headers(),
      withCredentials: true,
    });
  }

  deleteWithData<T>(endpoint: string, id: string, params?: Record<string, ApiParam>): Observable<T> {
    const httpParams = this.toParamSignal(params);
    return this.http.delete<T>(`${this.baseUrl}${endpoint}/${id}`, {
      params: httpParams(),
      headers: this.headers(),
      withCredentials: true,
    });
  }
}
