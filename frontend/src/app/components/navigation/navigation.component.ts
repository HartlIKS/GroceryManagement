import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, RouterOutlet, MatMenuTrigger, MatMenu, MatMenuItem, RouterLink],
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.css']
})
export class NavigationComponent {
  readonly authService = inject(AuthService);
  private readonly isMasterDataRoute_ = signal(false);
  readonly isMasterDataRoute = this.isMasterDataRoute_.asReadonly();

  constructor(private router: Router) {
    this.router.events.subscribe(() => {
      this.isMasterDataRoute_.set(this.router.url.startsWith('/master-data'));
    });
  }
}
