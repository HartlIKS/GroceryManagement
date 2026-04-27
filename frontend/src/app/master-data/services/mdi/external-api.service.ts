import { Injectable, isSignal, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, CacheService, GetApiEndpoint } from '../../../services';
import { CreateExternalAPIDTO, ExternalAPIDTO } from '../../models';
import { Page } from '../../../models';

@Injectable({
  providedIn: 'root'
})
export class ExternalAPIService extends CacheService<ExternalAPIDTO, CreateExternalAPIDTO> {
  private readonly endpoint = '/masterdata/interface';

  constructor(private apiService: ApiService) {
    super();
  }

  protected override rawGet(uuid: string): GetApiEndpoint<ExternalAPIDTO> {
    return this.apiService.getById<ExternalAPIDTO>(this.endpoint, uuid, false);
  }

  protected override rawUpdate(uuid: string, externalApi: CreateExternalAPIDTO): Observable<ExternalAPIDTO> {
    return this.apiService.put<ExternalAPIDTO>(this.endpoint, uuid, externalApi);
  }

  protected override rawDelete(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

  // Get external APIs with pagination and search
  getExternalAPIs(
    name: Signal<string> | string = '',
    page: Signal<number> | number = 0,
    size: Signal<number> | number = 20
  ) {
    return this.apiService.get<Page<ExternalAPIDTO>>(this.endpoint, {
        name,
        page,
        size,
    }, false);
  }

  // Get single external API by UUID
  getExternalAPI(uuid: Signal<string | undefined> | string) {
    if(isSignal(uuid)) return this.apiService.getById<ExternalAPIDTO>(this.endpoint, uuid, false);
    return this.get(uuid);
  }

  // Create external API
  createExternalAPI(externalApi: CreateExternalAPIDTO): Observable<ExternalAPIDTO> {
    return this.apiService.post<ExternalAPIDTO>(this.endpoint, externalApi);
  }
}
