import {
  Component,
  computed,
  effect,
  inject,
  InjectionToken,
  input,
  InputSignal,
  signal,
  Type,
  viewChild
} from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { MappingTableService } from '../../services';
import { MatInput } from '@angular/material/input';
import { NamedCacheService } from '../../../services';
import { MatPaginator } from '@angular/material/paginator';
import { NgComponentOutlet } from '@angular/common';

export const MAPPING_SERVICE_TOKEN = new InjectionToken<MappingTableService>('MappingService');

export interface MappingEntry {
  localId: string;
  remoteId: string;
}

export const ENTITY_SERVICE_TOKEN = new InjectionToken<NamedCacheService<{uuid: string, name: string}, any>>('EntityService');

export const ENTITY_DISPLAY_COMPONENT_TOKEN = new InjectionToken<Type<{
  readonly uuid: InputSignal<string>,
}>>('EntityDisplayComponent');

@Component({
  selector: 'app-mapping-list',
  standalone: true,
  imports: [
    MatTableModule,
    MatButtonModule,
    MatIcon,
    MatFormFieldModule,
    MatSelectModule,
    FormsModule,
    MatInput,
    MatPaginator,
    NgComponentOutlet
  ],
  templateUrl: './mapping-list.component.html',
  styleUrls: ['./mapping-list.component.css']
})
export class MappingListComponent {
  displayedColumns: string[] = ['localId', 'remoteId', 'actions'];

  readonly parentUuid = input.required<string>();
  private readonly mappingService = inject(MAPPING_SERVICE_TOKEN);
  protected readonly mappingResource = this.mappingService.getMappings(this.parentUuid);
  protected readonly mappings = computed(() => this.mappingResource.value() ?? {});
  private readonly entityService = inject(ENTITY_SERVICE_TOKEN);
  protected readonly entityDisplayComponent = inject(ENTITY_DISPLAY_COMPONENT_TOKEN, {
    optional: true,
  });

  protected readonly entitySearch = signal('');
  protected readonly newLocalId = signal<string | undefined>(undefined);
  protected readonly newRemoteId = signal<string | undefined>(undefined);
  private readonly availableEntitiesResource = this.entityService.search(this.entitySearch);
  protected readonly availableEntities = computed(() => this.availableEntitiesResource.value()?.content ?? []);

  private readonly paginator = viewChild(MatPaginator);

  public readonly dataSource = computed(() => {
    const mappingRecord = this.mappings();
    const entries: MappingEntry[] = Object.entries(mappingRecord).map(([localId, remoteId]) => ({
      localId,
      remoteId
    }));
    return new MatTableDataSource<MappingEntry>(entries);
  });

  private readonly eff = effect(() => {
    this.dataSource().paginator = this.paginator();
  });

  onAddMapping(): void {
    const localId = this.newLocalId();
    const remoteId = this.newRemoteId();
    const parentUuid = this.parentUuid();

    if (!localId || !remoteId || !parentUuid) return;

    this.mappingService.setInboundTranslation(parentUuid, remoteId, localId).subscribe({
      next: () => {
        this.newLocalId.set('');
        this.newRemoteId.set('');
      },
      error: (error) => {
        console.error(`Error adding mapping:`, error);
      }
    });
  }

  onDeleteMapping(localId: string, remoteId: string): void {
    const parentUuid = this.parentUuid();
    if (!parentUuid) return;

    if (confirm(`Are you sure you want to delete this mapping?`)) {
      // Delete would go here if we had a delete method
      console.log(`Delete mapping: ${localId} -> ${remoteId}`);
    }
  }
}
