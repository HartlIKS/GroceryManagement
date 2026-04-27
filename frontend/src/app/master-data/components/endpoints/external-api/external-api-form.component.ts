import { Component, computed, effect, inject, Injector, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import {
  ExternalAPIService,
  PriceEndpointService,
  ProductEndpointService,
  StoreEndpointService,
  ProductMappingTableService,
  StoreMappingTableService,
  ProductService,
  StoreService
} from '../../../services';
import { CreateExternalAPIDTO } from '../../../models';
import { ENDPOINT_SERVICE_TOKEN, EndpointListComponent } from '../endpoint-list.component';
import { MAPPING_SERVICE_TOKEN, ENTITY_SERVICE_TOKEN, MappingListComponent } from '../mapping-list.component';
import { NgTemplateOutlet } from '@angular/common';

@Component({
  selector: 'app-external-api-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatInput,
    MatProgressSpinner,
    RouterLink,
    EndpointListComponent,
    MappingListComponent,
    NgTemplateOutlet
  ],
  templateUrl: './external-api-form.component.html',
  styleUrls: ['./external-api-form.component.css']
})
export class ExternalAPIFormComponent implements OnInit {
  externalAPIForm: FormGroup;
  isEditing = computed(() => !!this.externalAPIId())
  externalAPIId = signal<string | undefined>(undefined);

  private readonly externalAPIService = inject(ExternalAPIService);

  protected readonly priceServiceInjector = Injector.create({
    parent: inject(Injector),
    providers: [{provide: ENDPOINT_SERVICE_TOKEN, useExisting: PriceEndpointService}],
  });
  protected readonly productServiceInjector = Injector.create({
    parent: inject(Injector),
    providers: [
      {provide: ENDPOINT_SERVICE_TOKEN, useExisting: ProductEndpointService},
      {provide: MAPPING_SERVICE_TOKEN, useExisting: ProductMappingTableService},
      {provide: ENTITY_SERVICE_TOKEN, useExisting: ProductService},
    ],
  });
  protected readonly storeServiceInjector = Injector.create({
    parent: inject(Injector),
    providers: [
      {provide: ENDPOINT_SERVICE_TOKEN, useExisting: StoreEndpointService},
      {provide: MAPPING_SERVICE_TOKEN, useExisting: StoreMappingTableService},
      {provide: ENTITY_SERVICE_TOKEN, useExisting: StoreService},
    ],
  });


  private readonly externalAPIResource = this.externalAPIService.getExternalAPI(this.externalAPIId);

  public readonly loading = computed(() => {
    const apiStatus = this.externalAPIResource.status();
    return apiStatus === 'loading';
  });
  public readonly error = computed(() => {
    const apiStatus = this.externalAPIResource.status();
    return apiStatus === 'error' ? 'Failed to load external API' : null;
  });

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.externalAPIForm = this.fb.group({
      name: ['', Validators.required],
      productMappings: [{}],
      storeMappings: [{}]
    });
    effect(() => {
      const externalAPI = this.externalAPIResource.value();
      if (externalAPI) {
        this.externalAPIForm.patchValue(externalAPI);
      }
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(({id}) => {
      this.externalAPIId.set(id);
    });
  }

  onSave(): void {
    if (this.externalAPIForm.invalid) {
      this.externalAPIForm.markAllAsTouched();
      return;
    }

    const externalAPIData: CreateExternalAPIDTO = this.externalAPIForm.value;
    const externalAPIId = this.externalAPIId();
    if (externalAPIId) {
      this.externalAPIService.update(externalAPIId, externalAPIData).subscribe({
        next: () => {
          this.router.navigate(['/master-data/external-api']);
        },
        error: (error) => {
          console.error('Error updating external API:', error);
        }
      });
    } else {
      this.externalAPIService.createExternalAPI(externalAPIData).subscribe({
        next: () => {
          this.router.navigate(['/master-data/external-api']);
        },
        error: (error) => {
          console.error('Error creating external API:', error);
        }
      });
    }
  }
}
