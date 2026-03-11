import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButton } from '@angular/material/button';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatIcon } from '@angular/material/icon';
import { ApiService } from '../../../services';
import { Share } from '../../../models';

@Component({
  selector: 'app-join-share',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButton,
    MatProgressSpinner,
    MatIcon,
    RouterLink
  ],
  templateUrl: './join-share.component.html',
  styleUrls: ['./join-share.component.css']
})
export class JoinShareComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly apiService = inject(ApiService);

  public joinLinkUuid: string | null = null;
  public loading = false;
  public error: string | null = null;
  public success = false;
  public shareInfo: Share | null = null;

  ngOnInit(): void {
    this.joinLinkUuid = this.route.snapshot.paramMap.get('uuid');

    if (!this.joinLinkUuid) {
      this.error = 'Invalid join link: No UUID provided';
      return;
    }

    // Auto-join when component loads
    this.joinShare();
  }

  joinShare(): void {
    if (!this.joinLinkUuid || this.loading) {
      return;
    }

    this.loading = true;
    this.error = null;

    this.apiService.post<Share>(`/share/join/${this.joinLinkUuid}`, undefined)
      .subscribe({
        next: (share) => {
          this.apiService.setShare(share.uuid);
          this.router.navigate(['/']);
        },
        error: (err) => {
          this.loading = false;
          if (err.status === 404) {
            this.error = 'Join link not found or has expired';
          } else if (err.status === 400) {
            this.error = 'This join link is no longer valid';
          } else {
            this.error = 'Failed to join share. Please try again later.';
          }
          console.error('Failed to join share:', err);
        }
      });
  }
}
