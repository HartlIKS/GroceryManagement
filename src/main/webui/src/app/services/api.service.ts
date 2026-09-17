import { computed, inject, Injectable, linkedSignal } from '@angular/core';
import { AuthService } from './auth.service';
import { HttpClient, HttpParams, HttpResourceRequest } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { EMPTY, Observable, of, switchMap, take } from 'rxjs';
import { toObservable } from '@angular/core/rxjs-interop';

export type ApiParam = string | Date | number | boolean | string[] | undefined;

export type ApiHeaders = Record<string, string | string[]> & {
  Authorization: `Bearer ${string}`,
  'X-Share-ID'?: string,
};

export type httpRequest = HttpResourceRequest & {
  headers: ApiHeaders,
};

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly authService = inject(AuthService);
  private readonly subject = computed((): string | undefined => this.authService.claims()?.['sub']);
  private readonly share_ = linkedSignal({
    source: this.subject,
    computation(sub): string | undefined {
      if (sub === undefined) return undefined;
      return sessionStorage.getItem(`share-${sub}`) ?? undefined;
    }
  });
  public readonly share = this.share_.asReadonly();
  public readonly headers = computed((): ApiHeaders | undefined => {
    const token = this.authService.accessToken();
    if (token === undefined) return undefined;
    const ret: ApiHeaders = {
      Authorization: `Bearer ${token}`,
    };
    const share = this.share();
    if (share) ret['X-Share-ID'] = share;
    return ret;
  });

  public setShare(share: string | undefined) {
    const sub = this.subject();
    if (sub === undefined) return;
    if (share === undefined) sessionStorage.removeItem(`share-${sub}`);
    else sessionStorage.setItem(`share-${sub}`, share);
    this.share_.set(share);
  }

  private toParams(params: Record<string, ApiParam> = {}): HttpParams {
    let httpParams = new HttpParams();
    for (const [key, value] of Object.entries(params)) {
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
    return httpParams;
  }

  // Generic CRUD operations
  get(endpoint: string, params?: Record<string, ApiParam>): httpRequest | undefined {
    const headers = this.headers();
    if (headers === undefined) return undefined;
    return {
      url: `${this.baseUrl}${endpoint}`,
      params: this.toParams(params),
      headers,
      credentials: 'include',
    };
  }

  getShareOnly(endpoint: string, params?: Record<string, ApiParam>): httpRequest | undefined {
    const ret = this.get(endpoint, params);
    if (ret?.headers?.["X-Share-ID"]) return ret;
    return undefined;
  }

  getById(endpoint: string, uuid: string): httpRequest | undefined {
    const headers = this.headers();
    if (headers === undefined) return undefined;
    return {
      url: `${this.baseUrl}${endpoint}/${uuid}`,
      headers,
      credentials: 'include',
    };
  }

  post<T>(endpoint: string, data: any): Observable<T> {
    return toObservable(this.headers)
      .pipe(
        switchMap(headers => headers ? of(headers) : EMPTY),
        take(1),
        switchMap(headers => {
          if (typeof data === 'string') {
            data = JSON.stringify(data);
            headers = {
              ...headers,
              'Content-Type': 'application/json',
            };
          }
          return this.http.post<T>(`${this.baseUrl}${endpoint}`, data, {
            headers,
            withCredentials: true,
          });
        })
      );
  }

  put<T>(endpoint: string, id: string, data: any): Observable<T> {
    return toObservable(this.headers)
      .pipe(
        switchMap(headers => headers ? of(headers) : EMPTY),
        take(1),
        switchMap(headers => {
          if (typeof data === 'string') {
            data = JSON.stringify(data);
            headers = {
              ...headers,
              'Content-Type': 'application/json',
            };
          }
          return this.http.put<T>(`${this.baseUrl}${endpoint}/${id}`, data, {
            headers,
            withCredentials: true,
          });
        })
      );
  }

  delete(endpoint: string, id: string, params?: Record<string, ApiParam>): Observable<void> {
    return toObservable(this.headers)
      .pipe(
        switchMap(headers => headers ? of(headers) : EMPTY),
        take(1),
        switchMap(headers => this.http.delete<void>(`${this.baseUrl}${endpoint}/${id}`, {
          params: this.toParams(params),
          headers,
          withCredentials: true,
        }))
      );
  }

  deleteWithData<T>(endpoint: string, id: string, params?: Record<string, ApiParam>): Observable<T> {
    return toObservable(this.headers)
      .pipe(
        switchMap(headers => headers ? of(headers) : EMPTY),
        take(1),
        switchMap(headers => this.http.delete<T>(`${this.baseUrl}${endpoint}/${id}`, {
          params: this.toParams(),
          headers,
          withCredentials: true,
        }))
      );
  }
}
