import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { CurrentShareService } from '../../services/current-share.service';
import { CreateShareDTO } from '../../models';
import { ShareService } from '../../services/share.service';

@Component({
  selector: 'app-share-admin',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIcon,
    MatProgressSpinner,
    RouterLink
  ],
  templateUrl: './share-admin.component.html',
  styleUrls: ['./share-admin.component.css']
})
export class ShareAdminComponent implements OnInit {
  private readonly currentShareService = inject(CurrentShareService);
  private readonly shareService = inject(ShareService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);

  public readonly currentShare = this.currentShareService.currentShareResource.value.asReadonly();
  public readonly loading = computed(() => this.currentShareService.currentShareResource.status() === 'loading');
  public readonly isEditMode = computed(() => this.currentShare() !== undefined);
  public readonly error = computed(() => {
    const status = this.currentShareService.currentShareResource.status();
    return status === 'error' ? 'Failed to load share data' : null;
  });

  public shareForm: FormGroup;
  public readonly isSubmitting = signal(false);

  constructor() {
    this.shareForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(3)]]
    });

    // Watch for changes in the current share data
    effect(() => {
      const share = this.currentShare();
      if (share) {
        this.shareForm.patchValue({
          name: share.name
        });
      }
    });
  }

  ngOnInit(): void {
    // Load current share data when component initializes
    this.currentShareService.currentShareResource.reload();
  }

  onSubmit(): void {
    if (this.shareForm.invalid || this.isSubmitting()) {
      return;
    }

    this.isSubmitting.set(true);
    const shareData: CreateShareDTO = { name: this.shareForm.value.name };

    if (this.isEditMode()) {
      // Update existing share
      this.currentShareService.updateCurrentShare(shareData)
        .subscribe({
          next: () => this.router.navigate(['/']),
          complete: () => this.isSubmitting.set(false),
        });
    } else {
      this.shareService.create(shareData)
        .subscribe({
          next: () => this.router.navigate(['/']),
          complete: () => this.isSubmitting.set(false),
        });
      this.isSubmitting.set(false);
    }
  }

  onDeleteShare(): void {
    if (this.isSubmitting() || !this.isEditMode()) {
      return;
    }

    const confirmDelete = confirm('Are you sure you want to delete this share? This action cannot be undone.');

    if (!confirmDelete) {
      return;
    }

    this.isSubmitting.set(true);
    this.currentShareService.deleteCurrentShare()
      .subscribe({
        next: () => this.router.navigate(['/']),
        complete: () => this.isSubmitting.set(false),
      });
  }
}
