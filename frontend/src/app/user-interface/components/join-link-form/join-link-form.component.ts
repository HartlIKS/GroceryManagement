import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { JoinLinkService } from '../../../services/join-link.service';
import { CreateJoinLinkDTO, Permissions } from '../../../models';

@Component({
  selector: 'app-join-link-form',
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
    MatSelectModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  providers: [DatePipe],
  templateUrl: './join-link-form.component.html',
  styleUrls: ['./join-link-form.component.css']
})
export class JoinLinkFormComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly formBuilder = inject(FormBuilder);
  private readonly joinLinkService = inject(JoinLinkService);

  public readonly joinLinkForm: FormGroup;
  public readonly isSubmitting = signal(false);
  public readonly isEditMode = computed(() => !!this.activatedRoute.snapshot.paramMap.get('id'));
  public readonly joinLinkId = computed(() => this.activatedRoute.snapshot.paramMap.get('id'));

  public readonly permissions: { value: Permissions; label: string }[] = [
    { value: 'NONE', label: 'No Access' },
    { value: 'READ', label: 'Read Only' },
    { value: 'WRITE', label: 'Read & Write' },
    { value: 'ADMIN', label: 'Administrator' }
  ];

  constructor() {
    this.joinLinkForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      permissions: ['READ' as Permissions, Validators.required],
      active: [true],
      singleUse: [false],
      validTo: [null]
    });

    // Load join link data if in edit mode
    effect(() => {
      const id = this.joinLinkId();
      if (id) {
        this.loadJoinLink(id);
      }
    });
  }

  ngOnInit(): void {
    // Form initialization is handled in constructor
  }

  private loadJoinLink(id: string): void {
    const joinLinks = this.joinLinkService.joinLinksResource.value() ?? [];
    const joinLink = joinLinks.find(link => link.uuid === id);

    if (joinLink) {
      this.joinLinkForm.patchValue({
        name: joinLink.name,
        permissions: joinLink.permissions,
        active: joinLink.active,
        singleUse: joinLink.singleUse,
        validTo: joinLink.validTo ? new Date(joinLink.validTo) : null
      });
    }
  }

  onSubmit(): void {
    if (this.joinLinkForm.invalid || this.isSubmitting()) {
      return;
    }

    this.isSubmitting.set(true);

    const formValue = this.joinLinkForm.value;
    const joinLinkData: CreateJoinLinkDTO = {
      name: formValue.name,
      permissions: formValue.permissions,
      active: formValue.active,
      singleUse: formValue.singleUse,
      validTo: formValue.validTo ? formValue.validTo.toISOString() : null
    };

    if (this.isEditMode()) {
      const id = this.joinLinkId();
      if (id) {
        this.joinLinkService.updateJoinLink(id, joinLinkData)
          .subscribe({
            next: () => {
              this.router.navigate(['/share-admin']);
            },
            error: (err) => {
              console.error('Failed to update join link:', err);
            },
            complete: () => this.isSubmitting.set(false),
          });
      }
    } else {
      this.joinLinkService.createJoinLink(joinLinkData)
        .subscribe({
          next: () => {
            this.router.navigate(['/share-admin']);
          },
          error: (err) => {
            console.error('Failed to create join link:', err);
          },
          complete: () => this.isSubmitting.set(false),
        });
    }
  }

  onCancel(): void {
    this.router.navigate(['/share-admin']);
  }
}
