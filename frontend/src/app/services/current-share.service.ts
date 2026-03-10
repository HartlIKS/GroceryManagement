import { inject, Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { CreateShareDTO, Share } from '../models';
import { ApiService } from './api.service';
import { ShareService } from './share.service';

@Injectable({
  providedIn: 'root'
})
export class CurrentShareService {
  private readonly apiService = inject(ApiService);
  private readonly shareService = inject(ShareService);

  public readonly currentShareResource = this.apiService.getShareOnly<Share>('/share/current');

  updateCurrentShare(shareData: CreateShareDTO): Observable<Share> {
    return this.apiService.put<Share>('/share', 'current', shareData)
      .pipe(tap(() => {
        this.currentShareResource.reload();
        this.shareService.shareResource.reload();
      }));
  }

  deleteCurrentShare(): Observable<void> {
    return this.apiService.delete('/share', 'current')
      .pipe(tap(() => this.apiService.setShare(undefined)));
  }
}
