import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-master-data-nav-items',
  standalone: true,
  imports: [CommonModule, MatIconModule, RouterLinkActive, RouterLink],
  templateUrl: './master-data-nav-items.component.html',
  styleUrls: ['./master-data-nav-items.component.css']
})
export class MasterDataNavItemsComponent {
}
