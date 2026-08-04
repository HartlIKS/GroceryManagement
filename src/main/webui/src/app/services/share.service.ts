import { computed, inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { CreateShareDTO, Share } from '../models';
import { tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ShareService {
  private readonly apiService = inject(ApiService);

  public readonly shareResource = this.apiService.get<Share[]>('/share');

  public readonly shareList = computed(() => this.shareResource.value() ?? []);

  create(shareData: CreateShareDTO) {
    return this.apiService.post<Share>('/share', shareData)
      .pipe(tap(() => this.shareResource.reload()));
  }
}
