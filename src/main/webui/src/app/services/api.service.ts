import { computed, inject, Injectable, Injector, linkedSignal, Signal } from '@angular/core';
import { AuthService } from './auth.service';
import { HttpClient, HttpParams, httpResource, HttpResourceRef } from '@angular/common/http';
import { resolve } from '../utils/signalutils';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

export type ApiParam = string | Date | number | boolean | string[] | undefined;

export type ApiHeaders = {
  Authorization: `Bearer ${string}`,
  'X-Share-ID'?: string,
};


@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly injector = inject(Injector);
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly authService = inject(AuthService);
  private readonly subject = computed((): string | undefined => this.authService.claims()?.['sub']);
  private readonly share_ = linkedSignal({
    source: this.subject,
    computation(sub): string | undefined {
      if(sub === undefined) return undefined;
      return sessionStorage.getItem(`share-${sub}`) ?? undefined;
    }
  });
  public readonly share = this.share_.asReadonly();
  public readonly headers = computed((): ApiHeaders | undefined => {
    const token = this.authService.accessToken();
    if(token === undefined) return undefined;
    const ret: ApiHeaders = {
      Authorization: `Bearer ${token}`,
    };
    const share = this.share();
    if(share) ret['X-Share-ID'] = share;
    return ret;
  });

  public setShare(share: string | undefined) {
    const sub = this.subject();
    if(sub === undefined) return;
    if(share === undefined) sessionStorage.removeItem(`share-${sub}`);
    else sessionStorage.setItem(`share-${sub}`, share);
    this.share_.set(share);
  }

  private toParamSignal(params: Record<string, Signal<ApiParam> | ApiParam> = {}): Signal<HttpParams | undefined> {
    const resolvedParams: Record<string, Signal<ApiParam>> = Object.fromEntries(
      Object.entries(params ?? {})
        .map(([k, v]) => [k, resolve(v)])
    );
    return computed(() => {
      let httpParams = new HttpParams();
      for (const [key, valueSignal] of Object.entries(resolvedParams)) {
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
      return httpParams;
    });
  }

  // Generic CRUD operations
  get<T>(endpoint: Signal<string | undefined> | string, params?: Record<string, Signal<ApiParam> | ApiParam>) {
    endpoint = resolve(endpoint);
    const httpParams = this.toParamSignal(params);
    return httpResource<T>(() => {
      const end = endpoint();
      const headers = this.headers();
      if(end === undefined || headers === undefined) return undefined;
      return {
        url: `${this.baseUrl}${end}`,
        params: httpParams(),
        headers,
        credentials: 'include',
      };
    }, {
      injector: this.injector,
    });
  }
  query<T>(endpoint: Signal<string | undefined> | string, body: any) {
    endpoint = resolve(endpoint);
    body = resolve(body);
    return httpResource<T>(() => {
      const end = endpoint();
      const headers = this.headers();
      const bodyValue = body();
      if(end === undefined || headers === undefined || bodyValue === undefined) return undefined;
      return {
        method: 'QUERY',
        url: `${this.baseUrl}${end}`,
        headers,
        credentials: 'include',
        body: bodyValue,
      };
    }, {
      injector: this.injector,
    });
  }

  getShareOnly<T>(endpoint: string, params?: Record<string, Signal<ApiParam> | ApiParam>) {
    const httpParams = this.toParamSignal(params);

    return httpResource<T>(() => {
      const params = httpParams();
      if((params?.get('share') ?? undefined) === undefined) return undefined;
      const headers = this.headers();
      if(headers === undefined) return undefined;
      return {
        url: `${this.baseUrl}${endpoint}`,
        params,
        headers,
        credentials: 'include',
      };
    }, {
      injector: this.injector,
    });
  }

  getById<T>(endpoint: string, id: Signal<string | undefined> | string): HttpResourceRef<T | undefined> {
    id = resolve(id);
    return httpResource<T>(() => {
      const uuid = id();
      if (uuid === undefined) return undefined;
      return {
        url: `${this.baseUrl}${endpoint}/${uuid}`,
        headers: this.headers(),
        credentials: 'include',
      };
    }, {
      injector: this.injector,
    });
  }

  post<T>(endpoint: string, data: any): Observable<T> {
    let headers: Record<string, string> | undefined = this.headers();
    if(typeof data === 'string') {
      data = JSON.stringify(data);
      headers = {
        ...(headers ?? {}),
        'Content-Type': 'application/json',
      };
    }
    return this.http.post<T>(`${this.baseUrl}${endpoint}`, data, {
      headers,
      withCredentials: true,
    });
  }

  put<T>(endpoint: string, id: string, data: any): Observable<T> {
    let headers: Record<string, string> | undefined = this.headers();
    if(typeof data === 'string') {
      data = JSON.stringify(data);
      headers = {
        ...(headers ?? {}),
        'Content-Type': 'application/json',
      };
    }
    return this.http.put<T>(`${this.baseUrl}${endpoint}/${id}`, data, {
      headers,
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
