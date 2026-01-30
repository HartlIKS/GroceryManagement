import { Component, computed, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { CommonModule } from '@angular/common';
import { ShoppingTripService } from '../../services';
import { CreateShoppingTripDTO, ListShoppingTripDTO } from '../../models';
import { ProductService, StoreService } from '../../../master-data/services';
import { MatTooltip } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-shopping-trip-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatTableModule,
    MatSelectModule,
    MatTooltip,
    MatDividerModule,
    RouterLink
  ],
  templateUrl: './shopping-trip-form.component.html'
})
export class ShoppingTripFormComponent implements OnInit {
  shoppingTripForm: FormGroup;
  productSelectControl: FormControl;
  isEditMode = false;
  shoppingTripUuid: string | null = null;

  // Product management properties
  availableProducts = computed(() => this.productService.products());
  shoppingTripProducts = signal<Record<string, number>>({});
  productAmountControls = signal<Record<string, FormControl>>({});

  // Store management
  availableStores = computed(() => this.storeService.stores());

  // Table properties
  displayedColumns: string[] = ['productName', 'image', 'amount', 'actions'];
  productsDataSource = computed(() => new MatTableDataSource(
    Object.entries(this.shoppingTripProducts()).map(([uuid, amount]) => {
      const product = this.availableProducts().find(p => p.uuid === uuid);
      return {
        uuid,
        amount,
        name: product?.name || 'Unknown Product',
        image: product?.image,
        type: 'product' as const
      };
    })
  ));

  // Loading and error states
  loading = computed(() => this.shoppingTripService.loading());
  error = computed(() => this.shoppingTripService.error());

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private shoppingTripService: ShoppingTripService,
    private productService: ProductService,
    private storeService: StoreService
  ) {
    this.shoppingTripForm = this.fb.group({
      store: ['', Validators.required],
      time: ['', Validators.required]
    });

    this.productSelectControl = new FormControl('');

    // Load initial data
    this.productService.getProducts();
    this.storeService.getStores();
  }

  ngOnInit(): void {
    // Check if we're in edit mode
    const uuid = this.route.snapshot.paramMap.get('id');
    if (uuid) {
      this.isEditMode = true;
      this.shoppingTripUuid = uuid;
      this.loadShoppingTrip(uuid);
    }
  }

  loadShoppingTrip(uuid: string): void {
    this.shoppingTripService.getShoppingTrip(uuid).subscribe({
      next: (shoppingTrip: ListShoppingTripDTO) => {
        this.shoppingTripForm.patchValue({
          store: shoppingTrip.store,
          time: this.formatDateTimeForInput(shoppingTrip.time)
        });
        this.shoppingTripProducts.set(shoppingTrip.products);
        this.initializeProductControls();
      },
      error: (error) => {
        console.error('Error loading shopping trip:', error);
      }
    });
  }

  addProduct(): void {
    const selectedUuid = this.productSelectControl.value;
    if (selectedUuid) {
      const currentProducts = this.shoppingTripProducts();
      const currentAmount = currentProducts[selectedUuid] || 0;

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

      const operation = this.isEditMode
        ? this.shoppingTripService.updateShoppingTrip(this.shoppingTripUuid!, shoppingTripData)
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

  getProductName(productUuid: string): string {
    const product = this.availableProducts().find(p => p.uuid === productUuid);
    return product ? product.name : 'Unknown Product';
  }

  getProductImage(productUuid: string): string | undefined {
    const product = this.availableProducts().find(p => p.uuid === productUuid);
    return product?.image;
  }

  getStoreName(storeUuid: string): string {
    const store = this.availableStores().find(s => s.uuid === storeUuid);
    return store ? store.name : 'Unknown Store';
  }

  getStoreLogo(storeUuid: string): string | undefined {
    const store = this.availableStores().find(s => s.uuid === storeUuid);
    return store?.logo;
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
