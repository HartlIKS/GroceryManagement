import { inject, ResourceParamsContext } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, httpRequest } from '../../../services';
import { Page } from '../../../models';
import { EndpointDTOTypes } from '../../models';
import { CREATE, LIST, UPDATE } from '../../../models/base.model';
import { httpResource, HttpResourceRef } from '@angular/common/http';

export abstract class EndpointService<T extends EndpointDTOTypes> {
  protected abstract readonly endpointType: string;
  protected readonly apiService = inject(ApiService);
  protected readonly baseEndpoint = '/masterdata/interface';

  protected rawGet(api: string, uuid: string): httpRequest | undefined {
    return this.apiService.get(`${this.baseEndpoint}/${api}/endpoint/${this.endpointType}/${uuid}`);
  }

  get(uuids: (ctx: ResourceParamsContext) => readonly [string, string] | undefined): HttpResourceRef<T[LIST] | undefined> {
    return httpResource(ctx => {
      const ids = uuids(ctx);
      if(ids) return this.rawGet(...ids);
      return undefined;
    });
  }

  create(parentUuid: string, data: T[CREATE]): Observable<T[LIST]> {
    return this.apiService.post<T[LIST]>(`${this.baseEndpoint}/${parentUuid}/endpoint/${this.endpointType}`, data);
  }

  update(parentUuid: string, uuid: string, data: T[UPDATE]): Observable<T[LIST]> {
    return this.apiService.put<T[LIST]>(`${this.baseEndpoint}/${parentUuid}/endpoint/${this.endpointType}`, uuid, data);
  }

  delete(parentUuid: string, uuid: string): Observable<void> {
    return this.apiService.delete(`${this.baseEndpoint}/${parentUuid}/endpoint/${this.endpointType}`, uuid);
  }

  search(q: (ctx: ResourceParamsContext) => {
    parentUuid: string,
    name: string,
    page?: number,
    size?: number
  } | undefined) {
    return httpResource<Page<T[LIST]>>(ctx => {
      const combined = q(ctx);
      if(combined) {
        const {parentUuid, ...query} = combined;
        return this.apiService.get(`${this.baseEndpoint}/${parentUuid}/endpoint/${this.endpointType}`, query);
      }
      return undefined;
    });
  }

  execUrl(parentUuid: string, uuid: string): string {
    return `${this.baseEndpoint}/${parentUuid}/endpoint/${this.endpointType}/${uuid}/exec`;
  }
}
