import { computed, inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { CreateShareDTO, Share } from '../models';
import { tap } from 'rxjs';
import { httpResource } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ShareService {
  private readonly apiService = inject(ApiService);

  public readonly shareResource = httpResource<readonly Share[]>(() => this.apiService.get('/share'), {
    defaultValue: [],
  });

  public readonly shareList = computed(() => this.shareResource.hasValue() ? this.shareResource.value() : []);

  create(shareData: CreateShareDTO) {
    return this.apiService.post<Share>('/share', shareData)
      .pipe(tap(() => this.shareResource.reload()));
  }
}
