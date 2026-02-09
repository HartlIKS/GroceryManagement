import { Component, computed, effect, inject, OnInit, signal, DestroyRef } from '@angular/core';
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
import { ProductGroupService, ShoppingListService } from '../../services';
import { CreateShoppingListDTO } from '../../models';
import { ProductService } from '../../../master-data/services';
import { MatTooltip } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-shopping-list-form',
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
  templateUrl: './shopping-list-form.component.html',
  styleUrls: ['./shopping-list-form.component.css']
})
export class ShoppingListFormComponent implements OnInit {
  shoppingListForm: FormGroup;
  isEditMode = computed(() => !!this.shoppingListUuid());
  shoppingListUuid = signal<string>('');

  // Create HTTP resources
  private readonly productsResource = inject(ProductService).getProducts('', 0, 1000);
  private readonly productGroupsResource = inject(ProductGroupService).getProductGroups('', 0, 1000);
  private readonly shoppingListResource = inject(ShoppingListService).getShoppingList(this.shoppingListUuid);

  // Product management properties
  selectedProductUuid: string | null = null;
  availableProducts = computed(() => this.productsResource.value()?.content ?? []);
  shoppingListProducts = signal<Record<string, number>>({});
  productAmountControls = signal<Record<string, FormControl>>({});

  // Product group management properties
  selectedProductGroupUuid: string | null = null;
  availableProductGroups = computed(() => this.productGroupsResource.value()?.content ?? []);
  shoppingListProductGroups = signal<Record<string, number>>({});
  productGroupAmountControls = signal<Record<string, FormControl>>({});

  // Unified selection properties
  selectedItemUuid: string | null = null;
  selectedItemType: 'product' | 'productGroup' | null = null;

  // Combined available items for dropdown
  availableItems = computed(() => {
    const products = this.availableProducts().map(p => ({ ...p, type: 'product' as const }));
    const groups = this.availableProductGroups().map(g => {
      // Get sample images from products in this group for dropdown
      const groupProducts = g.products || {};
      const availableProducts = this.availableProducts();

      const sampleImages = Object.keys(groupProducts)
        .slice(0, 2) // Take up to 2 sample images for dropdown
        .map(productUuid => availableProducts.find(p => p.uuid === productUuid)?.image)
        .filter(image => image); // Remove null/undefined images

      return { ...g, type: 'productGroup' as const, sampleImages };
    });
    return [...products, ...groups];
  });

  currentProducts = computed(() => {
    const products = this.shoppingListProducts();
    const availableProducts = this.availableProducts();
    return Object.entries(products).map(([uuid, amount]) => {
      const product = availableProducts.find(p => p.uuid === uuid);
      return {
        uuid,
        amount,
        name: product?.name ?? 'Unknown Product',
        image: product?.image,
        type: 'product' as const,
        EAN: product?.EAN
      };
    });
  });

  currentProductGroups = computed(() => {
    const groups = this.shoppingListProductGroups();
    const availableGroups = this.availableProductGroups();
    const availableProducts = this.availableProducts();

    return Object.entries(groups).map(([uuid, amount]) => {
      const group = availableGroups.find(g => g.uuid === uuid);

      // Get sample images from products in this group
      const groupProducts = group?.products || {};
      const sampleImages = Object.keys(groupProducts)
        .slice(0, 3) // Take up to 3 sample images
        .map(productUuid => {
          const product = availableProducts.find(p => p.uuid === productUuid);
          return product?.image;
        })
        .filter(image => image); // Remove null/undefined images;

      return {
        uuid,
        amount,
        name: group?.name ?? 'Unknown Group',
        image: null, // Product groups don't have single images
        type: 'productGroup' as const,
        sampleImages,
        productCount: Object.keys(groupProducts).length
      };
    });
  });

  // Combined data source for unified table
  combinedItems = computed(() => {
    const products = this.currentProducts();
    const groups = this.currentProductGroups();
    return [...products, ...groups];
  });

  // Table data sources
  combinedDataSource = computed(() => new MatTableDataSource(this.combinedItems()));

  // Loading and error states
  public readonly loading = computed(() =>
    this.productsResource.status() === 'loading' ||
    this.productGroupsResource.status() === 'loading' ||
    this.shoppingListResource.status() === 'loading'
  );
  public readonly error = computed(() => {
    const productStatus = this.productsResource.status();
    const groupStatus = this.productGroupsResource.status();
    const listStatus = this.shoppingListResource.status();
    return productStatus === 'error' || groupStatus === 'error' || listStatus === 'error'
      ? 'Failed to load data'
      : null;
  });

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private readonly destroyRef: DestroyRef,
  ) {
    this.shoppingListForm = this.fb.group({
      name: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    // Check if we're in edit mode
    this.route.params.subscribe(({id}) => {
      this.shoppingListUuid.set(id);
    });

    // Watch for changes in the shopping list resource
    effect(() => {
      const shoppingList = this.shoppingListResource.value();
      if (shoppingList) {
        this.shoppingListForm.patchValue({
          name: shoppingList.name
        });
        this.shoppingListProducts.set(shoppingList.products || {});
        this.shoppingListProductGroups.set(shoppingList.productGroups || {});
        this.updateProductControls();
        this.updateProductGroupControls();
      }
    });
  }

  // Product management methods
  addProduct(): void {
    if (this.selectedProductUuid) {
      const currentProducts = this.shoppingListProducts();
      const newProducts = { ...currentProducts };

      if (!newProducts[this.selectedProductUuid]) {
        newProducts[this.selectedProductUuid] = 1;
        this.shoppingListProducts.set(newProducts);
        this.updateProductControls();
      }

      this.selectedProductUuid = null;
    }
  }

  // Product group management methods
  addProductGroup(): void {
    if (this.selectedProductGroupUuid) {
      const currentGroups = this.shoppingListProductGroups();
      const newGroups = { ...currentGroups };

      if (!newGroups[this.selectedProductGroupUuid]) {
        newGroups[this.selectedProductGroupUuid] = 1;
        this.shoppingListProductGroups.set(newGroups);
        this.updateProductGroupControls();
      }

      this.selectedProductGroupUuid = null;
    }
  }

  // Unified add method
  addItem(): void {
    if (this.selectedItemUuid && this.selectedItemType) {
      if (this.selectedItemType === 'product') {
        this.selectedProductUuid = this.selectedItemUuid;
        this.addProduct();
      } else if (this.selectedItemType === 'productGroup') {
        this.selectedProductGroupUuid = this.selectedItemUuid;
        this.addProductGroup();
      }

      // Reset unified selection
      this.selectedItemUuid = null;
      this.selectedItemType = null;
    }
  }

  // Helper method to handle selection change
  onSelectionChange(): void {
    if (this.selectedItemUuid) {
      const selectedItem = this.availableItems().find(item => item.uuid === this.selectedItemUuid);
      this.selectedItemType = selectedItem?.type ?? null;
    } else {
      this.selectedItemType = null;
    }
  }

  removeProduct(uuid: string): void {
    const currentProducts = this.shoppingListProducts();
    const newProducts = { ...currentProducts };
    delete newProducts[uuid];
    this.shoppingListProducts.set(newProducts);
    this.updateProductControls();
  }

  removeProductGroup(uuid: string): void {
    const currentGroups = this.shoppingListProductGroups();
    const newGroups = { ...currentGroups };
    delete newGroups[uuid];
    this.shoppingListProductGroups.set(newGroups);
    this.updateProductGroupControls();
  }

  updateProductAmount(uuid: string, amount: number): void {
    if (amount > 0) {
      const currentProducts = this.shoppingListProducts();
      const newProducts = { ...currentProducts, [uuid]: amount };
      this.shoppingListProducts.set(newProducts);
    }
  }

  updateProductGroupAmount(uuid: string, amount: number): void {
    if (amount > 0) {
      const currentGroups = this.shoppingListProductGroups();
      const newGroups = { ...currentGroups, [uuid]: amount };
      this.shoppingListProductGroups.set(newGroups);
    }
  }

  private updateProductControls(): void {
    const products = this.shoppingListProducts();
    const controls: Record<string, FormControl> = {};

    Object.entries(products).forEach(([uuid, amount]) => {
      controls[uuid] = new FormControl(amount, [Validators.required, Validators.min(0.01)]);

      // Subscribe to value changes
      controls[uuid].valueChanges
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((newValue: number) => {
          if (newValue && newValue > 0) {
            this.updateProductAmount(uuid, newValue);
          }
        });
    });

    this.productAmountControls.set(controls);
  }

  private updateProductGroupControls(): void {
    const groups = this.shoppingListProductGroups();
    const controls: Record<string, FormControl> = {};

    Object.entries(groups).forEach(([uuid, amount]) => {
      controls[uuid] = new FormControl(amount, [Validators.required, Validators.min(0.01)]);

      // Subscribe to value changes
      controls[uuid].valueChanges
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((newValue: number) => {
          if (newValue && newValue > 0) {
            this.updateProductGroupAmount(uuid, newValue);
          }
        });
    });

    this.productGroupAmountControls.set(controls);
  }

  // Helper methods for unified table
  removeItem(item: any): void {
    if (item.type === 'product') {
      this.removeProduct(item.uuid);
    } else if (item.type === 'productGroup') {
      this.removeProductGroup(item.uuid);
    }
  }
  getItemAmountControl(item: any): FormControl {
    if (item.type === 'product') {
      return this.productAmountControls()[item.uuid] || new FormControl(0);
    } else if (item.type === 'productGroup') {
      return this.productGroupAmountControls()[item.uuid] || new FormControl(0);
    }
    return new FormControl(0);
  }

  onSave(): void {
    if (this.shoppingListForm.valid) {
      const formData: CreateShoppingListDTO = {
        name: this.shoppingListForm.value.name,
        products: this.shoppingListProducts(),
        productGroups: this.shoppingListProductGroups()
      };

      const shoppingListUuid = this.shoppingListUuid();
      const shoppingListService = inject(ShoppingListService);

      const operation = shoppingListUuid
        ? shoppingListService.updateShoppingList(shoppingListUuid, formData)
        : shoppingListService.createShoppingList(formData);

      operation.subscribe({
        next: () => {
          this.router.navigate(['/shopping-lists']);
        },
        error: (error) => {
          console.error('Error saving shopping list:', error);
        }
      });
    }
  }

}
