import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { PriceEndpointService } from '../../../services';
import { CreatePriceEndpointDTO } from '../../../models';

@Component({
  selector: 'app-price-endpoint-form',
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
  templateUrl: './price-endpoint-form.component.html',
  styleUrls: ['./price-endpoint-form.component.css']
})
export class PriceEndpointFormComponent implements OnInit {
  priceEndpointForm: FormGroup;
  isEditing = computed(() => !!this.priceEndpointId())
  priceEndpointId = signal<string | undefined>(undefined);
  parentUuid = signal<string | undefined>(undefined);

  private readonly priceEndpointService = inject(PriceEndpointService);

  private readonly priceEndpointResource = this.priceEndpointService.getEndpoint(this.parentUuid, this.priceEndpointId);

  public readonly loading = computed(() => {
    const endpointStatus = this.priceEndpointResource.status();
    return endpointStatus === 'loading';
  });
  public readonly error = computed(() => {
    const endpointStatus = this.priceEndpointResource.status();
    return endpointStatus === 'error' ? 'Failed to load price endpoint' : null;
  });

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.priceEndpointForm = this.fb.group({
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
      productHandling: this.fb.group({
        type: ['path']
      }),
      storeHandling: this.fb.group({
        type: ['path']
      }),
      pricePath: ['', Validators.required],
      timeFormat: [''],
      validFromPath: [''],
      validUntilPath: ['']
    });
    effect(() => {
      const priceEndpoint = this.priceEndpointResource.value();
      if (priceEndpoint) {
        this.priceEndpointForm.patchValue(priceEndpoint);
      }
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(({id, endpointId}) => {
      this.parentUuid.set(id);
      this.priceEndpointId.set(endpointId);
    });
  }

  onSave(): void {
    if (this.priceEndpointForm.invalid) {
      this.priceEndpointForm.markAllAsTouched();
      return;
    }

    const priceEndpointData: CreatePriceEndpointDTO = this.priceEndpointForm.value;
    const priceEndpointId = this.priceEndpointId();
    const parentUuid = this.parentUuid();
    if (priceEndpointId && parentUuid) {
      this.priceEndpointService.update(parentUuid, priceEndpointId, priceEndpointData).subscribe({
        next: () => {
          this.router.navigate(['/master-data/external-api', parentUuid]);
        },
        error: (error) => {
          console.error('Error updating price endpoint:', error);
        }
      });
    } else if(parentUuid) {
      this.priceEndpointService.create(parentUuid, priceEndpointData).subscribe({
        next: () => {
          this.router.navigate(['/master-data/external-api', parentUuid]);
        },
        error: (error) => {
          console.error('Error creating price endpoint:', error);
        }
      });
    }
  }
}
