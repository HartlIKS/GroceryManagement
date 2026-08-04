import { inject, Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { JoinLink, CreateJoinLinkDTO } from '../models';

@Injectable({
  providedIn: 'root'
})
export class JoinLinkService {
  private readonly apiService = inject(ApiService);

  public readonly joinLinksResource = this.apiService.getShareOnly<JoinLink[]>('/share/current/links');

  createJoinLink(joinLinkData: CreateJoinLinkDTO): Observable<JoinLink> {
    return this.apiService.post<JoinLink>('/share/current/links', joinLinkData)
      .pipe(tap(() => {
        this.joinLinksResource.reload();
      }));
  }

  deleteJoinLink(linkUuid: string): Observable<void> {
    return this.apiService.delete('/share/current/links', linkUuid)
      .pipe(tap(() => {
        this.joinLinksResource.reload();
      }));
  }

  updateJoinLink(linkUuid: string, joinLinkData: CreateJoinLinkDTO): Observable<JoinLink> {
    return this.apiService.put<JoinLink>('/share/current/links', linkUuid, joinLinkData)
      .pipe(tap(() => {
        this.joinLinksResource.reload();
      }));
  }
}
