import { Component, computed, inject, InjectionToken, input, resource, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { FormsModule } from '@angular/forms';
import { EndpointDTOTypes } from '../../models';
import { EndpointService } from '../../services';
import { MatPaginator } from '@angular/material/paginator';

export const ENDPOINT_SERVICE_TOKEN = new InjectionToken<EndpointService<EndpointDTOTypes>>('EndpointService');

@Component({
  selector: 'app-endpoint-list',
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
  templateUrl: './endpoint-list.component.html',
  styleUrls: ['./endpoint-list.component.css']
})
export class EndpointListComponent {
  protected readonly displayedColumns: readonly string[] = ['name', 'baseUrl', 'actions'];
  readonly parentUuid = input.required<string>();
  readonly endpointType = input.required<string>();

  protected readonly searchTerm = signal('');
  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(20);
  private readonly endpointService = inject(ENDPOINT_SERVICE_TOKEN);

  protected readonly endpointsResource = this.endpointService.search(_ => ({
    parentUuid: this.parentUuid(),
    name: this.searchTerm(),
    page: this.pageIndex(),
    pageSize: this.pageSize(),
  }));

  public readonly dataSource = resource({
    params: ({ chain }) => chain(this.endpointsResource)?.content,
    async loader({ params }) {
      return new MatTableDataSource(params);
    }
  });

  public readonly formPath = computed(() => `/master-data/external-api/${this.parentUuid()}/${this.endpointType()}`);

  onDeleteEndpoint(uuid: string): void {
    const pid = this.parentUuid();
    if(pid === undefined) return;
    if (confirm(`Are you sure you want to delete this ${this.endpointType()} endpoint?`)) {
      this.endpointService.delete(pid, uuid).subscribe({
        error: (error) => {
          console.error(`Error deleting ${this.endpointType()} endpoint:`, error);
        }
      });
    }
  }
}
