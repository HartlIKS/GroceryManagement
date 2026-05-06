import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { StoreService } from '../../services';
import { CreateStoreDTO } from '../../models';

@Component({
  selector: 'app-store-form',
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
  templateUrl: './store-form.component.html',
  styleUrls: ['./store-form.component.css']
})
export class StoreFormComponent implements OnInit {
  storeForm: FormGroup;
  isEditing = computed(() => !!this.storeId());
  storeId = signal<string>('');

  private readonly storeService = inject(StoreService);

  // Create HTTP resource for store
  private readonly storeResource = this.storeService.getStore(this.storeId);

  // Computed properties from resource
  public readonly loading = computed(() => {
    const storeStatus = this.storeResource.status();
    return storeStatus === 'loading';
  });
  public readonly error = computed(() => {
    const storeStatus = this.storeResource.status();
    return storeStatus === 'error' ? 'Failed to load store' : null;
  });

  constructor(
    private fb: FormBuilder,
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
        streetAndNumber: ['']
      }),
      currency: ['']
    });
    // Watch for changes in the store resource
    effect(() => {
      const store = this.storeResource.value();
      if (store) {
        this.storeForm.patchValue(store);
      }
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(({id}) => {
      this.storeId.set(id);
    });
  }

  onSave(): void {
    if (this.storeForm.invalid) {
      this.storeForm.markAllAsTouched();
      return;
    }

    const storeData: CreateStoreDTO = this.storeForm.value;
    const storeId = this.storeId();
    if (storeId) {
      this.storeService.update(storeId, storeData).subscribe({
        next: () => {
          this.router.navigate(['/master-data/stores']);
        },
        error: (error) => {
          console.error('Error updating store:', error);
        }
      });
    } else {
      this.storeService.createStore(storeData).subscribe({
        next: () => {
          this.router.navigate(['/master-data/stores']);
        },
        error: (error) => {
          console.error('Error creating store:', error);
        }
      });
    }
  }

}
