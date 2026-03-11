import { Component, computed, inject } from '@angular/core';
import { ActivatedRoute, IsActiveMatchOptions, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatIcon } from '@angular/material/icon';
import { AuthService } from '../../services/auth.service';
import { ShareService } from '../../services/share.service';
import { MatMenu, MatMenuContent, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';
import { ApiService } from '../../services';
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from '@angular/material/sidenav';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatToolbar } from '@angular/material/toolbar';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatListItem, MatNavList } from '@angular/material/list';

export type NavItem = {
  path: string | string[],
  linkOptions?: {exact: boolean} | IsActiveMatchOptions,
} & ({
  title: string,
  icon?: string,
} | {
  title?: string,
  icon: string,
});

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [
    MatIcon,
    RouterOutlet,
    RouterLink,
    MatMenuTrigger,
    MatMenu,
    MatMenuItem,
    MatSidenavContainer,
    MatSidenavContent,
    MatSidenav,
    RouterLinkActive,
    MatToolbar,
    MatButton,
    MatMenuContent,
    MatIconButton,
    MatNavList,
    MatListItem
  ],
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.css']
})
export class NavigationComponent {
  readonly authService = inject(AuthService);
  readonly apiService = inject(ApiService);
  readonly shareService = inject(ShareService);
  private readonly activeRoute = inject(ActivatedRoute);
  private readonly routeData = toSignal(this.activeRoute.data);
  readonly navItems = computed((): NavItem[] => this.routeData()?.['navItems'] ?? []);
  readonly isMasterDataRoute = computed((): boolean => this.routeData()?.['isMasterData'] ?? false);
}
