import { Component, computed, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { ProductGroupService } from '../services';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatCardModule, MatProgressSpinnerModule, RouterLink],
  templateUrl: './user-dashboard.component.html',
  styleUrls: ['./user-dashboard.component.css']
})
export class UserDashboardComponent implements OnInit {
  constructor(private productGroupService: ProductGroupService) {}

  // Computed properties for statistics
  readonly totalGroups = computed(() => this.productGroupService.productGroups().length);

  readonly totalProductsInGroups = computed(() => {
    return this.productGroupService.productGroups()
      .reduce((total, group) => total + (group.products ? Object.keys(group.products).length : 0), 0);
  });

  readonly loading = computed(() => this.productGroupService.loading());

  ngOnInit() {
    // Load product groups data when dashboard initializes
    this.productGroupService.getProductGroups();
  }
}
