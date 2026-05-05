import { Component, computed, inject, Injector, linkedSignal, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
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
  ProductMappingTableService,
  ProductService,
  StoreEndpointService,
  StoreMappingTableService,
  StoreService
} from '../../../services';
import { CreateExternalAPIDTO } from '../../../models';
import { ENDPOINT_SERVICE_TOKEN, EndpointListComponent } from '../endpoint-list.component';
import {
  ENTITY_DISPLAY_COMPONENT_TOKEN,
  ENTITY_SERVICE_TOKEN,
  MAPPING_SERVICE_TOKEN,
  MappingListComponent
} from '../mapping-list.component';
import { NgTemplateOutlet } from '@angular/common';
import { form, FormField, FormRoot, schema } from '@angular/forms/signals';
import { StoreListingComponent } from '../../../../user-interface/components/store-listing/store-listing.component';
import {
  ProductListingComponent
} from '../../../../user-interface/components/product-listing/product-listing.component';
import { firstValueFrom } from 'rxjs';

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
    NgTemplateOutlet,
    FormField,
    FormRoot
  ],
  templateUrl: './external-api-form.component.html',
  styleUrls: ['./external-api-form.component.css']
})
export class ExternalAPIFormComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  isEditing = computed(() => !!this.externalAPIId())
  externalAPIId = signal<string | undefined>(undefined);

  private readonly externalAPIService = inject(ExternalAPIService);

  protected readonly priceServiceInjector = Injector.create({
    parent: inject(Injector),
    providers: [
      {provide: ENDPOINT_SERVICE_TOKEN, useExisting: PriceEndpointService},
    ],
  });
  protected readonly productServiceInjector = Injector.create({
    parent: inject(Injector),
    providers: [
      {provide: ENDPOINT_SERVICE_TOKEN, useExisting: ProductEndpointService},
      {provide: MAPPING_SERVICE_TOKEN, useExisting: ProductMappingTableService},
      {provide: ENTITY_DISPLAY_COMPONENT_TOKEN, useExisting: ProductListingComponent},
      {provide: ENTITY_SERVICE_TOKEN, useExisting: ProductService},
    ],
  });
  protected readonly storeServiceInjector = Injector.create({
    parent: inject(Injector),
    providers: [
      {provide: ENDPOINT_SERVICE_TOKEN, useExisting: StoreEndpointService},
      {provide: MAPPING_SERVICE_TOKEN, useExisting: StoreMappingTableService},
      {provide: ENTITY_DISPLAY_COMPONENT_TOKEN, useExisting: StoreListingComponent},
      {provide: ENTITY_SERVICE_TOKEN, useExisting: StoreService},
    ],
  });


  private readonly externalAPIResource = this.externalAPIService.getExternalAPI(this.externalAPIId);
  protected readonly externalAPIForm = form(
    linkedSignal({
      source: this.externalAPIResource.value,
      computation(src, prev): CreateExternalAPIDTO {
        return src ?? prev?.value ?? {
          name: '',
        };
      }
    }),
    schema(() => {}),
    {
      submission: {
        action: () => this.onSave()
      }
    }
  );

  public readonly loading = computed(() => {
    const apiStatus = this.externalAPIResource.status();
    return apiStatus === 'loading';
  });
  public readonly error = computed(() => {
    const apiStatus = this.externalAPIResource.status();
    return apiStatus === 'error' ? 'Failed to load external API' : null;
  });

  ngOnInit(): void {
    this.route.params.subscribe(({id}) => {
      this.externalAPIId.set(id);
    });
  }

  async onSave(): Promise<void> {
    if (!this.externalAPIForm().valid()) {
      this.externalAPIForm().markAsTouched();
      return;
    }

    const externalAPIData: CreateExternalAPIDTO = this.externalAPIForm().value();
    const externalAPIId = this.externalAPIId();
    if (externalAPIId) {
      await firstValueFrom(this.externalAPIService.update(externalAPIId, externalAPIData));
    } else {
      await firstValueFrom(this.externalAPIService.createExternalAPI(externalAPIData));
    }
    await this.router.navigate(['/master-data/external-api']);
  }
}
