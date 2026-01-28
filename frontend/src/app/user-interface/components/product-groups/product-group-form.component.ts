import { Component, OnInit, computed, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { CommonModule } from '@angular/common';
import { ProductGroupService } from '../../services';
import { CreateProductGroupDTO, ListProductGroupDTO } from '../../models';
import { ProductService } from '../../../master-data/services';
import { ListProductDTO } from '../../../master-data/models';
import { map } from 'rxjs/operators';
import { MatTooltip } from '@angular/material/tooltip';

@Component({
  selector: 'app-product-group-form',
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
    MatTooltip
  ],
  templateUrl: './product-group-form.component.html',
  styleUrls: ['./product-group-form.component.css']
})
export class ProductGroupFormComponent implements OnInit {
  productGroupForm: FormGroup;
  isEditMode = false;
  productGroupUuid: string | null = null;

  // Product management properties
  selectedProductUuid: string | null = null;
  availableProducts = computed(() => this.productService.products());
  productGroupUuids = signal<string[]>([]);
  currentProducts = computed(() => {
    const allProducts = this.availableProducts();
    const groupUuids = this.productGroupUuids();

    if (!groupUuids || groupUuids.length === 0) {
      return [];
    }

    return groupUuids
      .map(uuid => allProducts.find(p => p.uuid === uuid))
      .filter(product => product !== undefined) as ListProductDTO[];
  });
  productsDataSource = computed(() => new MatTableDataSource<ListProductDTO>(this.currentProducts()));

  // Use computed signals from service
  public readonly loading = computed(() => this.productGroupService.loading());
  public readonly error = computed(() => this.productGroupService.error());

  constructor(
    private fb: FormBuilder,
    private productGroupService: ProductGroupService,
    private productService: ProductService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.productGroupForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(255)]]
    });
  }

  ngOnInit(): void {
    // Subscribe to route parameter changes
    this.route.paramMap.pipe(
      map(params => {
        const id = params.get('id');
        if (id === 'new') {
          return { isEditMode: false, uuid: null };
        } else if (id) {
          return { isEditMode: true, uuid: id };
        }
        return { isEditMode: false, uuid: null };
      })
    ).subscribe(({ isEditMode, uuid }) => {
      this.isEditMode = isEditMode;
      this.productGroupUuid = uuid;

      if (isEditMode && uuid) {
        // Load products first, then load the product group
        this.loadAvailableProducts();
        this.loadProductGroup(uuid);
      }
    });
  }

  loadAvailableProducts(): void {
    // Load all products to populate the dropdown
    this.productService.getProducts('', 0, 1000); // Load all products
  }

  loadProductGroup(uuid: string): void {
    this.productGroupService.getProductGroup(uuid).subscribe({
      next: (productGroup: ListProductGroupDTO) => {
        this.productGroupForm.patchValue({
          name: productGroup.name
        });
        // Set the product UUIDs - the computed signal will automatically update
        this.productGroupUuids.set(productGroup.products || []);
      },
      error: (error) => {
        console.error('Error loading product group:', error);
      }
    });
  }

  addProductToGroup(): void {
    if (!this.selectedProductUuid || !this.productGroupUuid) {
      return;
    }

    this.productGroupService.addProductToGroup(this.productGroupUuid, this.selectedProductUuid).subscribe({
      next: (updatedGroup: ListProductGroupDTO) => {
        // Update local state
        this.productGroupService.updateProductGroupInCache(updatedGroup);
        this.productGroupUuids.set(updatedGroup.products || []);
        this.selectedProductUuid = null;
      },
      error: (error) => {
        console.error('Error adding product to group:', error);
      }
    });
  }

  removeProductFromGroup(productUuid: string): void {
    if (!this.productGroupUuid) {
      return;
    }

    this.productGroupService.removeProductFromGroup(this.productGroupUuid, productUuid).subscribe({
      next: (updatedGroup: ListProductGroupDTO) => {
        // Update local state
        this.productGroupService.updateProductGroupInCache(updatedGroup);
        this.productGroupUuids.set(updatedGroup.products || []);
      },
      error: (error) => {
        console.error('Error removing product from group:', error);
      }
    });
  }

  onSubmit(): void {
    if (this.productGroupForm.invalid) {
      return;
    }

    const formData: CreateProductGroupDTO = {
      name: this.productGroupForm.value.name
    };

    if (this.isEditMode && this.productGroupUuid) {
      this.updateProductGroup(this.productGroupUuid, formData);
    } else {
      this.createProductGroup(formData);
    }
  }

  createProductGroup(productGroup: CreateProductGroupDTO): void {
    this.productGroupService.createProductGroup(productGroup).subscribe({
      next: (createdProductGroup: ListProductGroupDTO) => {
        // Optimistic update
        this.productGroupService.addProductGroupToCache(createdProductGroup);
        this.router.navigate(['/product-groups']);
      },
      error: (error) => {
        console.error('Error creating product group:', error);
      }
    });
  }

  updateProductGroup(uuid: string, productGroup: CreateProductGroupDTO): void {
    this.productGroupService.updateProductGroup(uuid, productGroup).subscribe({
      next: (updatedProductGroup: ListProductGroupDTO) => {
        // Optimistic update
        this.productGroupService.updateProductGroupInCache(updatedProductGroup);
        this.router.navigate(['/product-groups']);
      },
      error: (error) => {
        console.error('Error updating product group:', error);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/product-groups']);
  }

  getErrorMessage(controlName: string): string {
    const control = this.productGroupForm.get(controlName);
    if (control?.hasError('required')) {
      return 'This field is required';
    }
    if (control?.hasError('minlength')) {
      return 'Minimum length is 1 character';
    }
    if (control?.hasError('maxlength')) {
      return 'Maximum length is 255 characters';
    }
    return '';
  }
}
