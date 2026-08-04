import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInput } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatIcon } from '@angular/material/icon';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { CommonModule } from '@angular/common';
import { ShoppingTripService } from '../../services';
import { CreateShoppingTripDTO } from '../../models';
import { ProductService, StoreService } from '../../../master-data/services';
import { MatTooltip } from '@angular/material/tooltip';

@Component({
  selector: 'app-shopping-trip-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatInput,
    MatFormFieldModule,
    MatProgressSpinner,
    MatIcon,
    MatTableModule,
    MatSelectModule,
    MatTooltip,
    RouterLink
  ],
  templateUrl: './shopping-trip-form.component.html'
})
export class ShoppingTripFormComponent implements OnInit {
  shoppingTripForm: FormGroup;
  productSelectControl: FormControl;
  isEditMode = computed(() => !!this.shoppingTripUuid());
  shoppingTripUuid = signal<string>('');

  private readonly shoppingTripService = inject(ShoppingTripService);

  // Create HTTP resources
  private readonly productsResource = inject(ProductService).search('', 0, 1000);
  private readonly storesResource = inject(StoreService).search('', 0, 1000);
  private readonly shoppingTripResource = this.shoppingTripService.getShoppingTrip(this.shoppingTripUuid);

  // Product management properties
  availableProducts = computed(() => this.productsResource.value()?.content ?? []);
  shoppingTripProducts = signal<Record<string, number>>({});
  productAmountControls = signal<Record<string, FormControl>>({});

  // Store management
  availableStores = computed(() => this.storesResource.value()?.content ?? []);

  // Table properties
  displayedColumns: string[] = ['productName', 'image', 'amount', 'actions'];
  productsDataSource = computed(() => new MatTableDataSource(
    Object.entries(this.shoppingTripProducts()).map(([uuid, amount]) => {
      const product = this.availableProducts().find(p => p.uuid === uuid);
      return {
        uuid,
        amount,
        name: product?.name ?? 'Unknown Product',
        image: product?.image,
        type: 'product' as const
      };
    })
  ));

  // Loading and error states
  loading = computed(() => this.shoppingTripResource.status() === 'loading');
  error = computed(() => {
    const status = this.shoppingTripResource.status();
    return status === 'error' ? 'Failed to load shopping trip' : null;
  });

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.shoppingTripForm = this.fb.group({
      store: ['', Validators.required],
      time: ['', Validators.required]
    });

    this.productSelectControl = new FormControl('');

    // Watch for changes in the shopping trip resource
    effect(() => {
      const shoppingTrip = this.shoppingTripResource.value();
      if (shoppingTrip) {
        this.shoppingTripForm.patchValue({
          store: shoppingTrip.store,
          time: this.formatDateTimeForInput(shoppingTrip.time)
        });
        this.shoppingTripProducts.set(shoppingTrip.products);
        this.initializeProductControls();
      }
    });
  }

  ngOnInit(): void {
    // Check if we're in edit mode
    this.route.params.subscribe(({id}) => {
      this.shoppingTripUuid.set(id);
    });
  }

  addProduct(): void {
    const selectedUuid = this.productSelectControl.value;
    if (selectedUuid) {
      const currentProducts = this.shoppingTripProducts();
      const currentAmount = currentProducts[selectedUuid] ?? 0;

      this.shoppingTripProducts.set({
        ...currentProducts,
        [selectedUuid]: currentAmount + 1
      });

      this.initializeProductControls();
      this.productSelectControl.setValue('');
    }
  }

  removeProduct(productUuid: string): void {
    const currentProducts = { ...this.shoppingTripProducts() };
    delete currentProducts[productUuid];
    this.shoppingTripProducts.set(currentProducts);

    const currentControls = { ...this.productAmountControls() };
    delete currentControls[productUuid];
    this.productAmountControls.set(currentControls);
  }

  updateProductAmount(productUuid: string, amount: number): void {
    if (amount <= 0) {
      this.removeProduct(productUuid);
    } else {
      const currentProducts = this.shoppingTripProducts();
      this.shoppingTripProducts.set({
        ...currentProducts,
        [productUuid]: amount
      });
    }
  }

  private initializeProductControls(): void {
    const controls: Record<string, FormControl> = {};
    Object.entries(this.shoppingTripProducts()).forEach(([uuid, amount]) => {
      controls[uuid] = new FormControl(amount, [Validators.required, Validators.min(0.01)]);
    });
    this.productAmountControls.set(controls);
  }

  onSubmit(): void {
    if (this.shoppingTripForm.valid) {
      const formValue = this.shoppingTripForm.value;
      const shoppingTripData: CreateShoppingTripDTO = {
        store: formValue.store,
        time: new Date(formValue.time).toISOString(),
        products: this.shoppingTripProducts()
      };

      const shoppingTripUuid = this.shoppingTripUuid();
      const operation = shoppingTripUuid
        ? this.shoppingTripService.update(shoppingTripUuid, shoppingTripData)
        : this.shoppingTripService.createShoppingTrip(shoppingTripData);

      operation.subscribe({
        next: () => {
          this.router.navigate(['/shopping-trips']);
        },
        error: (error) => {
          console.error('Error saving shopping trip:', error);
        }
      });
    }
  }

  private formatDateTimeForInput(dateString: string): string {
    const date = new Date(dateString);
    // Format as YYYY-MM-DDTHH:MM for datetime-local input
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }
}
