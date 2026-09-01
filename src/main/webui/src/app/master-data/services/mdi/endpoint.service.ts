import { computed, inject, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../services';
import { Page } from '../../../models';
import { resolve } from '../../../utils/signalutils';
import { EndpointDTOTypes } from '../../models';
import { CREATE, LIST, UPDATE } from '../../../models/base.model';

export abstract class EndpointService<T extends EndpointDTOTypes> {
  protected abstract readonly endpointType: string;
  private readonly apiService = inject(ApiService);
  private readonly baseEndpoint = '/masterdata/interface';

  getEndpoints(
    parentUuid: Signal<string | undefined> | string,
    name: Signal<string> | string = '',
    page: Signal<number> | number = 0,
    size: Signal<number> | number = 20
  ) {
    parentUuid = resolve(parentUuid);
    return this.apiService.get<Page<T[LIST]>>(computed(() => {
      const pid = parentUuid();
      if(pid === undefined || pid === null) return undefined;
      return `${this.baseEndpoint}/${parentUuid()}/endpoint/${this.endpointType}`;
    }), {
        name,
        page,
        size,
    }, false);
  }

  // Get single endpoint by UUID
  getEndpoint(parentUuid: Signal<string | undefined> | string, uuid: Signal<string | undefined> | string) {
    parentUuid = resolve(parentUuid);
    uuid = resolve(uuid);
    return this.apiService.get<T[LIST]>(computed(() => {
      const pid = parentUuid();
      const id = uuid();
      if(pid === undefined || id === undefined) return undefined;
      return `${this.baseEndpoint}/${pid}/endpoint/${this.endpointType}/${id}`;
    }));
  }

  // Create endpoint
  create(parentUuid: string, data: T[CREATE]): Observable<T[LIST]> {
    return this.apiService.post<T[LIST]>(`${this.baseEndpoint}/${parentUuid}/endpoint/${this.endpointType}`, data);
  }

  // update endpoint
  update(parentUuid: string, uuid: string, data: T[UPDATE]): Observable<T[LIST]> {
    return this.apiService.put<T[LIST]>(`${this.baseEndpoint}/${parentUuid}/endpoint/${this.endpointType}`, uuid, data);
  }

  // delete endpoint
  delete(parentUuid: string, uuid: string): Observable<void> {
    return this.apiService.delete(`${this.baseEndpoint}/${parentUuid}/endpoint/${this.endpointType}`, uuid);
  }

  execUrl(parentUuid: string, uuid: string): string {
    return `${this.baseEndpoint}/${parentUuid}/endpoint/${this.endpointType}/${uuid}/exec`;
  }
}
