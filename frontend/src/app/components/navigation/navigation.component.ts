import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { MatIcon } from '@angular/material/icon';
import { AuthService } from '../../services/auth.service';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [MatIcon, RouterOutlet, RouterLink, MatMenuTrigger, MatMenu, MatMenuItem],
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
