import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { StoreEndpointService } from '../../../services';
import { CreateStoreEndpointDTO } from '../../../models';

@Component({
  selector: 'app-store-endpoint-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInput,
    MatProgressSpinner,
    MatSelectModule,
    RouterLink
  ],
  templateUrl: './store-endpoint-form.component.html',
  styleUrls: ['./store-endpoint-form.component.css']
})
export class StoreEndpointFormComponent implements OnInit {
  storeEndpointForm: FormGroup;
  isEditing = computed(() => !!this.storeEndpointId())
  storeEndpointId = signal<string | undefined>(undefined);
  parentUuid = signal<string | undefined>(undefined);

  private readonly storeEndpointService = inject(StoreEndpointService);

  private readonly storeEndpointResource = this.storeEndpointService.getEndpoint(this.parentUuid, this.storeEndpointId);

  public readonly loading = computed(() => {
    const endpointStatus = this.storeEndpointResource.status();
    return endpointStatus === 'loading';
  });
  public readonly error = computed(() => {
    const endpointStatus = this.storeEndpointResource.status();
    return endpointStatus === 'error' ? 'Failed to load store endpoint' : null;
  });

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.storeEndpointForm = this.fb.group({
      name: ['', Validators.required],
      baseUrl: ['', Validators.required],
      pageSize: this.fb.group({
        header: [''],
        queryParameter: ['']
      }),
      page: this.fb.group({
        header: [''],
        queryParameter: ['']
      }),
      itemCount: this.fb.group({
        header: [''],
        queryParameter: ['']
      }),
      basePath: ['', Validators.required],
      storeIdPath: ['', Validators.required],
      storeNamePath: ['', Validators.required],
      storeLogoPath: [''],
      addressPath: ['', Validators.required],
      addressPaths: this.fb.group({
        countryPath: [''],
        cityPath: [''],
        zipPath: [''],
        streetPath: [''],
        numberPath: ['']
      }),
      storeCurrencyPath: ['']
    });
    effect(() => {
      const storeEndpoint = this.storeEndpointResource.value();
      if (storeEndpoint) {
        this.storeEndpointForm.patchValue(storeEndpoint);
      }
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(({id, endpointId}) => {
      this.parentUuid.set(id);
      this.storeEndpointId.set(endpointId);
    });
  }

  onSave(): void {
    if (this.storeEndpointForm.invalid) {
      this.storeEndpointForm.markAllAsTouched();
      return;
    }

    const storeEndpointData: CreateStoreEndpointDTO = this.storeEndpointForm.value;
    const storeEndpointId = this.storeEndpointId();
    const parentUuid = this.parentUuid();
    if (storeEndpointId && parentUuid) {
      this.storeEndpointService.update(parentUuid, storeEndpointId, storeEndpointData).subscribe({
        next: () => {
          this.router.navigate(['/master-data/external-api', parentUuid]);
        },
        error: (error) => {
          console.error('Error updating store endpoint:', error);
        }
      });
    } else if(parentUuid) {
      this.storeEndpointService.create(parentUuid, storeEndpointData).subscribe({
        next: () => {
          this.router.navigate(['/master-data/external-api', parentUuid]);
        },
        error: (error) => {
          console.error('Error creating store endpoint:', error);
        }
      });
    }
  }
}
