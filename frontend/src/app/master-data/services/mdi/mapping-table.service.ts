import { computed, inject, Signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../services';
import { HttpResourceRef } from '@angular/common/http';
import { resolve } from '../../../utils/signalutils';

export abstract class MappingTableService {
  protected abstract readonly endpoint1: string;
  protected abstract readonly endpoint2: string;
  private readonly apiService = inject(ApiService);

  // Translate inbound: remote ID -> local UUID
  translateInbound(uuid: Signal<string | undefined> | string, remoteId: Signal<string | undefined> | string): HttpResourceRef<string | undefined> {
    uuid = resolve(uuid);
    remoteId = resolve(remoteId);
    return this.apiService.get<string>(computed(() => {
      const uuidValue = uuid();
      const remoteIdValue = remoteId();
      if(uuidValue === undefined || remoteIdValue === undefined) return undefined;
      return `${this.endpoint1}/${uuidValue}/mapping/${this.endpoint2}/in/${remoteIdValue}`;
    }));
  }

  // Set inbound translation: remote ID -> local UUID
  setInboundTranslation(uuid: string, remoteId: string, localId: string): Observable<string> {
    return this.apiService.put<string>(`${this.endpoint1}/${uuid}/mapping/${this.endpoint2}/in`, remoteId, localId);
  }

  // Translate outbound: local UUID -> remote ID
  translateOutbound(uuid: Signal<string | undefined> | string, localId: Signal<string | undefined> | string): HttpResourceRef<string | undefined> {
    uuid = resolve(uuid);
    localId = resolve(localId);
    return this.apiService.get<string>(computed(() => {
      const uuidValue = uuid();
      const localIdValue = localId();
      if(uuidValue === undefined || localIdValue === undefined) return undefined;
      return `${this.endpoint1}/${uuidValue}/mapping/${this.endpoint2}/in/${localIdValue}`;
    }));
  }

  // Set outbound translation: local UUID -> remote ID
  setOutboundTranslation(uuid: string, localId: string, remoteId: string): Observable<string> {
    return this.apiService.put<string>(`${this.endpoint1}/${uuid}/mapping/${this.endpoint2}/out`, localId, remoteId);
  }
}
