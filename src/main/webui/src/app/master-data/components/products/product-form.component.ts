import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { ProductService } from '../../services';
import { ProductTypes } from '../../models';
import { UPDATE } from '../../../models/base.model';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInput,
    MatProgressSpinner,
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
  protected readonly productResource = this.productService.get(this.productId);

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.productForm = this.fb.group({
      name: ['', Validators.required],
      version: [0],
      image: [''],
      EAN: ['']
    });
    // Watch for changes in the product resource
    effect(() => {
      if (this.productResource.hasValue()) {
        this.productForm.patchValue(this.productResource.value());
      }
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(({id}) => {
      this.productId.set(id);
    });
  }

  onSave(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    const productData: ProductTypes[UPDATE] = this.productForm.value;
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
      this.productService.create(productData).subscribe({
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
