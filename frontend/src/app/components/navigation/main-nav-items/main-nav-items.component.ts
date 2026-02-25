import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-main-nav-items',
  standalone: true,
  imports: [CommonModule, MatIconModule, RouterLinkActive, RouterLink],
  templateUrl: './main-nav-items.component.html',
  styleUrls: ['./main-nav-items.component.css']
})
export class MainNavItemsComponent {
}
