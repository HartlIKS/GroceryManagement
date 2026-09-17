import { httpResource, HttpResourceRef } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiParam, ApiService, httpRequest } from './api.service';
import { Page } from '../models';
import { inject, ResourceParamsContext } from '@angular/core';
import { BaseDTOTypes, CREATE, LIST, UPDATE } from '../models/base.model';
import { ListStoreDTO } from '../master-data/models';

export abstract class CrudService<T extends BaseDTOTypes> {
  protected readonly apiService = inject(ApiService);
  protected abstract endpoint: string;

  public rawGet(uuid: string): httpRequest | undefined {
    return this.apiService.getById(this.endpoint, uuid);
  }

  public get(uuid: (ctx: ResourceParamsContext) => string | undefined): HttpResourceRef<T[LIST] | undefined> {
    return httpResource(ctx => {
      const id = uuid(ctx);
      if(id) return this.rawGet(id);
      return undefined;
    });
  }

  public update(uuid: string, update: T[UPDATE]): Observable<T[LIST]> {
    return this.apiService.put(this.endpoint, uuid, update);
  }

  public create(create: T[CREATE]): Observable<T[LIST]> {
    return this.apiService.post(this.endpoint, create);
  }

  public delete(uuid: string, params?: Record<string, ApiParam>): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid, params);
  }
}

export abstract class NamedCrudService<T extends BaseDTOTypes> extends CrudService<T> {
  public search(q: (ctx: ResourceParamsContext) => {
    name: string,
    page?: number,
    size?: number,
  } | undefined): HttpResourceRef<Page<T[LIST]> | undefined> {
    return httpResource<Page<ListStoreDTO>>(ctx => {
      const query = q(ctx);
      if(query) return this.apiService.get(this.endpoint, query);
      return undefined;
    });
  }
}
