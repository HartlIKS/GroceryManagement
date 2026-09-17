import { inject, ResourceParamsContext } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../services';
import { httpResource, HttpResourceRef } from '@angular/common/http';

export abstract class MappingTableService {
  protected abstract readonly endpoint1: string;
  protected abstract readonly endpoint2: string;
  protected readonly apiService = inject(ApiService);

  getMappings(uuid: (ctx: ResourceParamsContext) => string | undefined) {
    return httpResource<Record<string, string>>(ctx => {
      const id = uuid(ctx);
      if(id) return this.apiService.get(`${this.endpoint1}/${id}/mapping/${this.endpoint2}`);
      return undefined;
    }, {
      defaultValue: {},
    });
  }

  translateInbound(ids: (ctx: ResourceParamsContext) => [string, string] | undefined) {
    return httpResource<string>(ctx => {
      const combined = ids(ctx);
      if(!combined) return undefined;
      const [uuid, remoteId] = combined;
      return this.apiService.get(`${this.endpoint1}/${uuid}/mapping/${this.endpoint2}/in/${remoteId}`);
    });
  }

  setInboundTranslation(uuid: string, remoteId: string, localId: string) {
    return this.apiService.put<string>(`${this.endpoint1}/${uuid}/mapping/${this.endpoint2}/in`, remoteId, localId);
  }

  translateOutbound(ids: (ctx: ResourceParamsContext) => [string, string] | undefined): HttpResourceRef<string | undefined> {
    return httpResource<string>(ctx => {
      const combined = ids(ctx);
      if(!combined) return undefined;
      const [uuid, localId] = combined;
      return this.apiService.get(`${this.endpoint1}/${uuid}/mapping/${this.endpoint2}/out/${localId}`);
    });
  }

  setOutboundTranslation(uuid: string, localId: string, remoteId: string): Observable<string> {
    return this.apiService.put<string>(`${this.endpoint1}/${uuid}/mapping/${this.endpoint2}/out`, localId, remoteId);
  }
}
