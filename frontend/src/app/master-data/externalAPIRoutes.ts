import { Routes } from '@angular/router';
import {
  ENDPOINT_TOKEN,
  EndpointConfig,
  EndpointTestComponent,
  ExternalAPIFormComponent,
  ExternalAPIListComponent,
  PriceEndpointFormComponent,
  ProductDiffComponent,
  ProductEndpointFormComponent,
  StoreDiffComponent,
  StoreEndpointFormComponent
} from './components/endpoints';
import {
  AddressDTO,
  AddressPathsDTO,
  ListProductDTO,
  ListStoreDTO,
  ProductEndpointDTO,
  StoreEndpointDTO
} from './models';
import { ProductEndpointService, StoreEndpointService } from './services';
import { inject } from '@angular/core';
import jp from 'jsonpath';

function toAddress(a: any, paths: AddressPathsDTO): AddressDTO {
  return {
    street: jp.value(a, paths.streetPath),
    number: jp.value(a, paths.numberPath),
    zip: jp.value(a, paths.zipPath),
    city: jp.value(a, paths.cityPath),
    country: jp.value(a, paths.countryPath)
  };
}

export const externalAPIRoutes: Routes = [
  {
    path: '',
    component: ExternalAPIListComponent
  },
  {
    path: 'new',
    component: ExternalAPIFormComponent
  },
  {
    path: ':id',
    component: ExternalAPIFormComponent
  },
  {
    path: ':id/price-endpoint/new',
    component: PriceEndpointFormComponent
  },
  {
    path: ':id/price-endpoint/:endpointId',
    component: PriceEndpointFormComponent
  },
  {
    path: ':id/product-endpoint/new',
    component: ProductEndpointFormComponent
  },
  {
    path: ':id/product-endpoint/:endpointId',
    component: ProductEndpointFormComponent
  },
  {
    path: ':id/store-endpoint/new',
    component: StoreEndpointFormComponent
  },
  {
    path: ':id/store-endpoint/:endpointId',
    component: StoreEndpointFormComponent
  },
  {
    path: ':id/product-endpoint/:endpointId/test',
    component: EndpointTestComponent,
    providers: [
      {
        provide: ENDPOINT_TOKEN,
        useFactory: (): EndpointConfig<ProductEndpointDTO, ListProductDTO> => ({
          endpointService: inject(ProductEndpointService),
          toPartials: (endpoint, response) => jp.query(response, endpoint.basePath).map(p => {
            const ret: Partial<ListProductDTO> & {uuid: string} = {
              uuid: jp.value(p, endpoint.productIdPath),
            };
            if(endpoint.productNamePath) ret.name = jp.value(p, endpoint.productNamePath);
            if(endpoint.productImagePath) ret.image = jp.value(p, endpoint.productImagePath);
            if(endpoint.productEANPath) ret.EAN = jp.value(p, endpoint.productEANPath);
            return ret;
          }),
          diffComponent: ProductDiffComponent,
        })
      }
    ],
  },
  {
    path: ':id/store-endpoint/:endpointId/test',
    component: EndpointTestComponent,
    providers: [
      {
        provide: ENDPOINT_TOKEN,
        useFactory: (): EndpointConfig<StoreEndpointDTO, ListStoreDTO> => ({
          endpointService: inject(StoreEndpointService),
          toPartials: (endpoint, response) => jp.query(response, endpoint.basePath).map(p => {
            const ret: Partial<ListStoreDTO> & {uuid: string} = {
              uuid: jp.value(p, endpoint.storeIdPath)
            };
            if(endpoint.storeNamePath) ret.name = jp.value(p, endpoint.storeNamePath);
            if(endpoint.storeLogoPath) ret.logo = jp.value(p, endpoint.storeLogoPath);
            if(endpoint.addressPath) ret.address = toAddress(jp.value(p, endpoint.addressPath), endpoint.addressPaths);
            if(endpoint.storeCurrencyPath) ret.currency = jp.value(p, endpoint.storeCurrencyPath);
            return ret;
          }),
          diffComponent: StoreDiffComponent,
        })
      }
    ]
  }
];
