import { computed, inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Share } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ShareService {
  private readonly apiService = inject(ApiService);

  public readonly shareResource = this.apiService.get<Share[]>('/share');

  public readonly shareList = computed(() => this.shareResource.value() ?? []);
}
