import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { FormsModule } from '@angular/forms';
import { ExternalAPIService } from '../../../services';
import { ExternalAPIDTO } from '../../../models';

@Component({
  selector: 'app-external-api-list',
  standalone: true,
  imports: [
    MatTableModule,
    MatButtonModule,
    MatIcon,
    MatInput,
    MatFormFieldModule,
    MatProgressSpinner,
    FormsModule,
    RouterLink
  ],
  templateUrl: './external-api-list.component.html',
  styleUrls: ['./external-api-list.component.css']
})
export class ExternalAPIListComponent {
  displayedColumns: string[] = ['name', 'actions'];

  protected readonly searchTerm = signal('');

  private readonly externalAPIService = inject(ExternalAPIService);

  protected readonly externalAPIsResource = this.externalAPIService.getExternalAPIs(this.searchTerm());

  public readonly externalAPIs = computed(() => this.externalAPIsResource.value()?.content ?? []);
  public readonly loading = computed(() => this.externalAPIsResource.status() === 'loading');
  public readonly error = computed(() => {
    const status = this.externalAPIsResource.status();
    return status === 'error' ? 'Failed to load external APIs' : null;
  });

  public readonly dataSource = computed(() => {
    const externalAPIs = this.externalAPIs();
    return new MatTableDataSource<ExternalAPIDTO>(externalAPIs);
  });

  onDeleteExternalAPI(uuid: string): void {
    if (confirm('Are you sure you want to delete this external API?')) {
      this.externalAPIService.delete(uuid).subscribe({
        error: (error) => {
          console.error('Error deleting external API:', error);
        }
      });
    }
  }
}
