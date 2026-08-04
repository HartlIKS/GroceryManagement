import { Component, computed, inject, InjectionToken, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { FormsModule } from '@angular/forms';
import { EndpointDTO } from '../../models';
import { EndpointService } from '../../services';

export const ENDPOINT_SERVICE_TOKEN = new InjectionToken<EndpointService<EndpointDTO, unknown>>('EndpointService');

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
    RouterLink
  ],
  templateUrl: './endpoint-list.component.html',
  styleUrls: ['./endpoint-list.component.css']
})
export class EndpointListComponent {
  displayedColumns: string[] = ['name', 'baseUrl', 'actions'];

  protected readonly searchTerm = signal('');
  readonly parentUuid = input.required<string>();
  readonly endpointType = input.required<string>();
  private readonly endpointService = inject(ENDPOINT_SERVICE_TOKEN);

  protected readonly endpointsResource = this.endpointService.getEndpoints(this.parentUuid, this.searchTerm);

  public readonly endpoints = computed(() => this.endpointsResource.value()?.content ?? []);
  public readonly loading = computed(() => this.endpointsResource.status() === 'loading');
  public readonly error = computed(() => {
    const status = this.endpointsResource.status();
    return status === 'error' ? `Failed to load ${this.endpointType()} endpoints` : null;
  });

  public readonly dataSource = computed(() => {
    const endpoints = this.endpoints();
    return new MatTableDataSource(endpoints);
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
