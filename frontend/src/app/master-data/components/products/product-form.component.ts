import { Component, OnInit, computed } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../services';
import { CreateProductDTO, ListProductDTO } from '../../models';

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
    MatProgressSpinnerModule
  ],
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.css']
})
export class ProductFormComponent implements OnInit {
  productForm: FormGroup;
  isEditing = false;
  productId?: string;

  // Use computed signals from service
  public readonly loading = computed(() => this.productService.loading());
  public readonly error = computed(() => this.productService.error());

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
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
    this.productId = this.route.snapshot.paramMap.get('id') || undefined;
    if (this.productId) {
      this.isEditing = true;
      this.loadProduct();
    }
  }

  loadProduct(): void {
    if (!this.productId) return;

    this.productService.getProduct(this.productId).subscribe({
      next: (product: ListProductDTO) => {
        this.productForm.patchValue({
          name: product.name,
          image: product.image || '',
          EAN: product.EAN || ''
        });
      },
      error: (error) => {
        console.error('Error loading product:', error);
      }
    });
  }

  onSave(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    const productData: CreateProductDTO = this.productForm.value;

    if (this.isEditing && this.productId) {
      this.productService.updateProduct(this.productId, productData).subscribe({
        next: (updatedProduct) => {
          // Optimistic update
          this.productService.updateProductInCache(updatedProduct);
          this.router.navigate(['/products']);
        },
        error: (error) => {
          console.error('Error updating product:', error);
        }
      });
    } else {
      this.productService.createProduct(productData).subscribe({
        next: (newProduct) => {
          // Optimistic update
          this.productService.addProductToCache(newProduct);
          this.router.navigate(['/products']);
        },
        error: (error) => {
          console.error('Error creating product:', error);
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/products']);
  }
}
