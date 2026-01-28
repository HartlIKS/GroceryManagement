import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

interface ApiParams {
  [key: string]: string | number | boolean | string[] | undefined;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly baseUrl: string;
  public readonly loading = signal(false);
  public readonly error = signal<string | null>(null);

  constructor(private http: HttpClient) {
    this.baseUrl = environment.apiUrl || 'http://localhost:8080';
  }

  private getUrl(endpoint: string): string {
    return `${this.baseUrl}${endpoint}`;
  }

  private setLoading(loading: boolean): void {
    this.loading.set(loading);
  }

  private setError(error: string | null): void {
    this.error.set(error);
  }

  // Generic CRUD operations
  get<T>(endpoint: string, params?: ApiParams): Observable<T> {
    this.setLoading(true);
    this.setError(null);
    
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach(key => {
        const value = params[key];
        if (value !== null && value !== undefined) {
          if (Array.isArray(value)) {
            value.forEach(item => httpParams = httpParams.append(key, String(item)));
          } else {
            httpParams = httpParams.set(key, String(value));
          }
        }
      });
    }
    
    return this.http.get<T>(this.getUrl(endpoint), { params: httpParams }).pipe(
      tap({
        next: () => {
          this.setLoading(false);
          this.setError(null);
        },
        error: (error: any) => {
          this.setLoading(false);
          this.setError(error.message || 'An error occurred');
        }
      })
    );
  }

  getById<T>(endpoint: string, id: string): Observable<T> {
    this.setLoading(true);
    this.setError(null);
    
    return this.http.get<T>(this.getUrl(`${endpoint}/${id}`)).pipe(
      tap({
        next: () => {
          this.setLoading(false);
          this.setError(null);
        },
        error: (error: any) => {
          this.setLoading(false);
          this.setError(error.message || 'An error occurred');
        }
      })
    );
  }

  post<T>(endpoint: string, data: any): Observable<T> {
    this.setLoading(true);
    this.setError(null);
    
    return this.http.post<T>(this.getUrl(endpoint), data).pipe(
      tap({
        next: () => {
          this.setLoading(false);
          this.setError(null);
        },
        error: (error: any) => {
          this.setLoading(false);
          this.setError(error.message || 'An error occurred');
        }
      })
    );
  }

  put<T>(endpoint: string, id: string, data: any): Observable<T> {
    this.setLoading(true);
    this.setError(null);
    
    return this.http.put<T>(this.getUrl(`${endpoint}/${id}`), data).pipe(
      tap({
        next: () => {
          this.setLoading(false);
          this.setError(null);
        },
        error: (error: any) => {
          this.setLoading(false);
          this.setError(error.message || 'An error occurred');
        }
      })
    );
  }

  delete(endpoint: string, id: string): Observable<void> {
    this.setLoading(true);
    this.setError(null);
    
    return this.http.delete<void>(this.getUrl(`${endpoint}/${id}`)).pipe(
      tap({
        next: () => {
          this.setLoading(false);
          this.setError(null);
        },
        error: (error: any) => {
          this.setLoading(false);
          this.setError(error.message || 'An error occurred');
        }
      })
    );
  }

  deleteWithData<T>(endpoint: string, id: string): Observable<T> {
    this.setLoading(true);
    this.setError(null);
    
    return this.http.delete<T>(this.getUrl(`${endpoint}/${id}`)).pipe(
      tap({
        next: () => {
          this.setLoading(false);
          this.setError(null);
        },
        error: (error: any) => {
          this.setLoading(false);
          this.setError(error.message || 'An error occurred');
        }
      })
    );
  }
}
