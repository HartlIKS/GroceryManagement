import { computed, Injectable, Signal } from '@angular/core';
import { HttpClient, HttpParams, httpResource } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { resolve } from '../utils/signalutils';

type ApiParam = string | Date | number | boolean | string[] | undefined;

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly baseUrl: string;

  constructor(private http: HttpClient) {
    this.baseUrl = environment.apiUrl;
  }

  // Generic CRUD operations
  get<T>(endpoint: string, params?: Record<string, Signal<ApiParam> | ApiParam>) {
    const resolvedParams: Record<string, Signal<ApiParam>> = Object.fromEntries(
      Object.entries(params ?? {})
        .map(([k, v]) => [k, resolve(v)])
    );
    const httpParams = computed(() => {
      let httpParams = new HttpParams();
      for (const [key, valueSignal] of Object.entries(resolvedParams)) {
        let value = valueSignal();
        if (value !== null && value !== undefined) {
          if (Array.isArray(value)) {
            for(const item of value) httpParams = httpParams.append(key, String(item));
          } else if(value instanceof Date) {
            httpParams = httpParams.set(key, value.toISOString());
          } else {
            httpParams = httpParams.set(key, String(value));
          }
        }
      }
      return httpParams;
    });

    return httpResource<T>(() => ({
      url: `${this.baseUrl}${endpoint}`,
      params: httpParams(),
    }));
  }

  getById<T>(endpoint: string, id: Signal<string> | string) {
    id = resolve(id);
    return httpResource<T>(() => `${this.baseUrl}${endpoint}/${id()}`);
  }

  post<T>(endpoint: string, data: any): Observable<T> {
    return this.http.post<T>(`${this.baseUrl}${endpoint}`, data);
  }

  put<T>(endpoint: string, id: string, data: any): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}${`${endpoint}/${id}`}`, data);
  }

  delete(endpoint: string, id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}${`${endpoint}/${id}`}`);
  }

  deleteWithData<T>(endpoint: string, id: string): Observable<T> {
    return this.http.delete<T>(`${this.baseUrl}${`${endpoint}/${id}`}`);
  }
}
