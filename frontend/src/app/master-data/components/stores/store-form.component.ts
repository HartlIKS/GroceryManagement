import { Component, computed, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { StoreService } from '../../services';
import { CreateStoreDTO, ListStoreDTO } from '../../models';

@Component({
  selector: 'app-store-form',
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
  templateUrl: './store-form.component.html',
  styleUrls: ['./store-form.component.css']
})
export class StoreFormComponent implements OnInit {
  storeForm: FormGroup;
  isEditing = false;
  storeId?: string;

  // Use computed signals from service
  public readonly loading = computed(() => this.storeService.loading());
  public readonly error = computed(() => this.storeService.error());

  constructor(
    private fb: FormBuilder,
    private storeService: StoreService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.storeForm = this.fb.group({
      name: ['', Validators.required],
      logo: [''],
      address: this.fb.group({
        country: [''],
        city: [''],
        zip: [''],
        street: [''],
        number: ['']
      }),
      currency: ['']
    });
  }

  ngOnInit(): void {
    this.storeId = this.route.snapshot.paramMap.get('id') || undefined;
    if (this.storeId) {
      this.isEditing = true;
      this.loadStore();
    }
  }

  loadStore(): void {
    if (!this.storeId) return;

    this.storeService.getStore(this.storeId).subscribe({
      next: (store: ListStoreDTO) => {
        this.storeForm.patchValue({
          name: store.name,
          logo: store.logo || '',
          address: {
            country: store.address.country || '',
            city: store.address.city || '',
            zip: store.address.zip || '',
            street: store.address.street || '',
            number: store.address.number || ''
          },
          currency: store.currency || ''
        });
      },
      error: (error) => {
        console.error('Error loading store:', error);
      }
    });
  }

  onSave(): void {
    if (this.storeForm.invalid) {
      this.storeForm.markAllAsTouched();
      return;
    }

    const storeData: CreateStoreDTO = this.storeForm.value;

    if (this.isEditing && this.storeId) {
      this.storeService.updateStore(this.storeId, storeData).subscribe({
        next: (updatedStore) => {
          // Optimistic update
          this.storeService.updateStoreInCache(updatedStore);
          this.router.navigate(['/stores']);
        },
        error: (error) => {
          console.error('Error updating store:', error);
        }
      });
    } else {
      this.storeService.createStore(storeData).subscribe({
        next: (newStore) => {
          // Optimistic update
          this.storeService.addStoreToCache(newStore);
          this.router.navigate(['/stores']);
        },
        error: (error) => {
          console.error('Error creating store:', error);
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/stores']);
  }
}
