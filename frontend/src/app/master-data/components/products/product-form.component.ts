import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../services';
import { CreateProductDTO } from '../../models';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    RouterLink
  ],
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.css']
})
export class ProductFormComponent implements OnInit {
  productForm: FormGroup;
  isEditing = computed(() => !!this.productId())
  productId = signal<string>('');

  private readonly productService = inject(ProductService);

  // Create HTTP resource for product
  private readonly productResource = this.productService.getProduct(this.productId);

  // Computed properties from resource
  public readonly loading = computed(() => {
    const productStatus = this.productResource.status();
    return productStatus === 'loading';
  });
  public readonly error = computed(() => {
    const productStatus = this.productResource.status();
    return productStatus === 'error' ? 'Failed to load product' : null;
  });

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.productForm = this.fb.group({
      name: ['', Validators.required],
      image: [''],
      EAN: ['']
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(({id}) => {
      this.productId.set(id);
    });
    // Watch for changes in the product resource
    effect(() => {
      const product = this.productResource.value();
      if (product) {
        this.productForm.patchValue({
          name: product.name,
          image: product.image || '',
          EAN: product.EAN || ''
        });
      }
    });
  }

  onSave(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    const productData: CreateProductDTO = this.productForm.value;
    const productId = this.productId();
    if (productId) {
      this.productService.update(productId, productData).subscribe({
        next: () => {
          this.router.navigate(['/master-data/products']);
        },
        error: (error) => {
          console.error('Error updating product:', error);
        }
      });
    } else {
      this.productService.createProduct(productData).subscribe({
        next: () => {
          this.router.navigate(['/master-data/products']);
        },
        error: (error) => {
          console.error('Error creating product:', error);
        }
      });
    }
  }

}
