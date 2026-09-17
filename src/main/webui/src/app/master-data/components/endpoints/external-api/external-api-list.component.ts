import { Component, inject, resource, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { FormsModule } from '@angular/forms';
import { ExternalAPIService } from '../../../services';
import { MatPaginator } from '@angular/material/paginator';

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
    RouterLink,
    MatPaginator
  ],
  templateUrl: './external-api-list.component.html',
  styleUrls: ['./external-api-list.component.css']
})
export class ExternalAPIListComponent {
  protected readonly displayedColumns: readonly string[] = ['name', 'actions'];

  protected readonly searchTerm = signal('');
  protected readonly page = signal(0);
  protected readonly size = signal(20);

  private readonly externalAPIService = inject(ExternalAPIService);

  protected readonly externalAPIsResource = this.externalAPIService.search(_ => ({
    name: this.searchTerm(),
    page: this.page(),
    size: this.size(),
  }));

  public readonly dataSource = resource({
    params: ({chain}) => chain(this.externalAPIsResource)?.content,
    async loader({params}) {
      return new MatTableDataSource(params);
    }
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
