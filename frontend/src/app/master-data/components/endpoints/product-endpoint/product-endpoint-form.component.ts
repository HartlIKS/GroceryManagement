import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { ProductEndpointService } from '../../../services';
import { CreateProductEndpointDTO } from '../../../models';

@Component({
  selector: 'app-product-endpoint-form',
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
  templateUrl: './product-endpoint-form.component.html',
  styleUrls: ['./product-endpoint-form.component.css']
})
export class ProductEndpointFormComponent implements OnInit {
  productEndpointForm: FormGroup;
  isEditing = computed(() => !!this.productEndpointId())
  productEndpointId = signal<string | undefined>(undefined);
  parentUuid = signal<string | undefined>(undefined);

  private readonly productEndpointService = inject(ProductEndpointService);

  private readonly productEndpointResource = this.productEndpointService.getEndpoint(this.parentUuid, this.productEndpointId);

  public readonly loading = computed(() => {
    const endpointStatus = this.productEndpointResource.status();
    return endpointStatus === 'loading';
  });
  public readonly error = computed(() => {
    const endpointStatus = this.productEndpointResource.status();
    return endpointStatus === 'error' ? 'Failed to load product endpoint' : null;
  });

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.productEndpointForm = this.fb.group({
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
      productIdPath: ['', Validators.required],
      productNamePath: ['', Validators.required],
      productImagePath: [''],
      productEANPath: ['']
    });
    effect(() => {
      const productEndpoint = this.productEndpointResource.value();
      if (productEndpoint) {
        this.productEndpointForm.patchValue(productEndpoint);
      }
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(({id, endpointId}) => {
      this.parentUuid.set(id);
      this.productEndpointId.set(endpointId);
    });
  }

  onSave(): void {
    if (this.productEndpointForm.invalid) {
      this.productEndpointForm.markAllAsTouched();
      return;
    }

    const productEndpointData: CreateProductEndpointDTO = this.productEndpointForm.value;
    const productEndpointId = this.productEndpointId();
    const parentUuid = this.parentUuid();
    if (productEndpointId && parentUuid) {
      this.productEndpointService.update(parentUuid, productEndpointId, productEndpointData).subscribe({
        next: () => {
          this.router.navigate(['/master-data/external-api', parentUuid]);
        },
        error: (error) => {
          console.error('Error updating product endpoint:', error);
        }
      });
    } else if(parentUuid) {
      this.productEndpointService.create(parentUuid, productEndpointData).subscribe({
        next: () => {
          this.router.navigate(['/master-data/external-api', parentUuid]);
        },
        error: (error) => {
          console.error('Error creating product endpoint:', error);
        }
      });
    }
  }
}
