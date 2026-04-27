import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { PriceService, ProductService, StoreService } from '../../services';
import { CreatePriceListingDTO, UpdatePriceDTO } from '../../models';

@Component({
  selector: 'app-price-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInput,
    MatSelectModule,
    MatProgressSpinner,
    RouterLink
  ],
  templateUrl: './price-form.component.html',
  styleUrls: ['./price-form.component.css']
})
export class PriceFormComponent implements OnInit {
  priceForm: FormGroup;
  isEditing = computed(() => !!this.priceId());
  priceId = signal<string>('');

  private readonly productService = inject(ProductService);
  private readonly storeService = inject(StoreService);
  private readonly priceService = inject(PriceService);

  // Create HTTP resources
  private readonly productsResource = this.productService.search('', 0, 1000);
  private readonly storesResource = this.storeService.search('', 0, 1000);
  private readonly priceResource = this.priceService.getPrice(this.priceId);

  // Computed properties from resources
  public readonly products = computed(() => this.productsResource.value()?.content ?? []);
  public readonly stores = computed(() => this.storesResource.value()?.content ?? []);
  public readonly loading = computed(() => {
    const priceStatus = this.priceResource.status();
    return priceStatus === 'loading';
  });
  public readonly error = computed(() => {
    const priceStatus = this.priceResource.status();
    return priceStatus === 'error' ? 'Failed to load price' : null;
  });

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.priceForm = this.fb.group({
      product: ['', Validators.required],
      store: ['', Validators.required],
      price: ['', [Validators.required, Validators.min(0)]],
      validFrom: ['', Validators.required],
      validTo: ['']
    });
    effect(() => {
      const price = this.priceResource.value();
      if (price) {
        this.priceForm.patchValue({
          ...price,
          validFrom: this.convertToDateTimeLocal(price.validFrom),
          validTo: this.convertToDateTimeLocal(price.validTo) || ''
        });
      }
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(({id}) => {
      this.priceId.set(id);
    })
  }

  onSave(): void {
    if (this.priceForm.invalid) {
      this.priceForm.markAllAsTouched();
      return;
    }

    let priceId = this.priceId();

    if (priceId) {
      const updateData: UpdatePriceDTO = {
        price: this.priceForm.value.price,
        validFrom: this.convertToIsoDateTime(this.priceForm.value.validFrom),
        validTo: this.priceForm.value.validTo ? this.convertToIsoDateTime(this.priceForm.value.validTo) : undefined
      };

      this.priceService.update(priceId, updateData).subscribe({
        next: () => {
          this.router.navigate(['/master-data/prices']);
        },
        error: (error: any) => {
          console.error('Error updating price:', error);
        }
      });
    } else {
      const createData: CreatePriceListingDTO = {
        product: this.priceForm.value.product,
        store: this.priceForm.value.store,
        price: this.priceForm.value.price,
        validFrom: this.convertToIsoDateTime(this.priceForm.value.validFrom) ?? '',
        validTo: this.convertToIsoDateTime(this.priceForm.value.validTo) ?? ''
      };

      this.priceService.createPrice(createData).subscribe({
        next: () => {
          this.router.navigate(['/master-data/prices']);
        },
        error: (error: any) => {
          console.error('Error creating price:', error);
        }
      });
    }
  }


  private convertToIsoDateTime(dateString: string | undefined): string | undefined {
    if (!dateString) return dateString;
    // datetime-local gives us YYYY-MM-DDTHH:mm in local timezone
    // We need to treat this as local time and convert to ISO properly
    const date = new Date(dateString);
    return date.toISOString();
  }

  private convertToDateTimeLocal(isoString: string | undefined): string | undefined {
    if (!isoString) return isoString;
    // Convert ISO string to YYYY-MM-DDTHH:mm format for datetime-local input
    // Preserve the local time representation
    const date = new Date(isoString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }
}
