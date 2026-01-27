import { Component, computed, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { PriceService, ProductService, StoreService } from '../../services';
import { CreatePriceListingDTO, ListPriceDTO, UpdatePriceDTO } from '../../models';

@Component({
  selector: 'app-price-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './price-form.component.html',
  styleUrls: ['./price-form.component.css']
})
export class PriceFormComponent implements OnInit {
  priceForm: FormGroup;
  isEditing = false;
  priceId?: string;

  // Use computed signals from services
  public readonly products = computed(() => this.productService.products());
  public readonly stores = computed(() => this.storeService.stores());
  public readonly loading = computed(() => this.priceService.loading());
  public readonly error = computed(() => this.priceService.error());

  constructor(
    private fb: FormBuilder,
    private priceService: PriceService,
    private productService: ProductService,
    private storeService: StoreService,
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
  }

  ngOnInit(): void {
    this.loadProducts();
    this.loadStores();

    this.priceId = this.route.snapshot.paramMap.get('id') || undefined;
    if (this.priceId) {
      this.isEditing = true;
      this.loadPrice();
    }
  }

  loadProducts(): void {
    this.productService.getProducts('', 0, 1000);
  }

  loadStores(): void {
    this.storeService.getStores('', 0, 1000);
  }

  loadPrice(): void {
    if (!this.priceId) return;

    this.priceService.getPrice(this.priceId).subscribe({
      next: (price: ListPriceDTO) => {
        this.priceForm.patchValue({
          product: price.product,
          store: price.store,
          price: price.price,
          validFrom: this.convertToDateTimeLocal(price.validFrom),
          validTo: this.convertToDateTimeLocal(price.validTo) || ''
        });
      },
      error: (error: any) => {
        console.error('Error loading price:', error);
      }
    });
  }

  onSave(): void {
    if (this.priceForm.invalid) {
      this.priceForm.markAllAsTouched();
      return;
    }

    if (this.isEditing && this.priceId) {
      const updateData: UpdatePriceDTO = {
        price: this.priceForm.value.price,
        validFrom: this.convertToIsoDateTime(this.priceForm.value.validFrom),
        validTo: this.priceForm.value.validTo ? this.convertToIsoDateTime(this.priceForm.value.validTo) : undefined
      };

      this.priceService.updatePrice(this.priceId, updateData).subscribe({
        next: (updatedPrice) => {
          // Optimistic update
          this.priceService.updatePriceInCache(updatedPrice);
          this.router.navigate(['/prices']);
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
        next: (newPrice) => {
          // Optimistic update
          this.priceService.addPriceToCache(newPrice);
          this.router.navigate(['/prices']);
        },
        error: (error: any) => {
          console.error('Error creating price:', error);
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/prices']);
  }

  private convertToIsoDateTime(dateString: string | undefined): string | undefined {
    if (!dateString) return dateString;
    // datetime-local gives us YYYY-MM-DDTHH:mm, we need to add timezone info
    const date = new Date(dateString);
    return date.toISOString();
  }

  private convertToDateTimeLocal(isoString: string | undefined): string | undefined {
    if (!isoString) return isoString;
    // Convert ISO string to YYYY-MM-DDTHH:mm format for datetime-local input
    const date = new Date(isoString);
    // Get the local datetime representation in the format required by datetime-local input
    return date.toISOString().slice(0, 16);
  }
}
