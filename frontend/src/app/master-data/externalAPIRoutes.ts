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

function jsonValue(a: any, path?: string): any {
  if(!path) return undefined;
  const e = /^'([^']*)'$/.exec(path);
  if(e) return e[1];
  return jp.value(a, path);
}

function toAddress(a: any, paths: AddressPathsDTO): AddressDTO {
  return {
    street: jsonValue(a, paths.streetPath),
    number: jsonValue(a, paths.numberPath),
    zip: jsonValue(a, paths.zipPath),
    city: jsonValue(a, paths.cityPath),
    country: jsonValue(a, paths.countryPath),
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
            return {
              uuid: jsonValue(p, endpoint.productIdPath),
              name: jsonValue(p, endpoint.productNamePath),
              image: jsonValue(p, endpoint.productImagePath),
              EAN: jsonValue(p, endpoint.productEANPath),
            };
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
            return {
              uuid: jsonValue(p, endpoint.storeIdPath),
              name: jsonValue(p, endpoint.storeNamePath),
              logo: jsonValue(p, endpoint.storeLogoPath),
              address: toAddress(jsonValue(p, endpoint.addressPath), endpoint.addressPaths),
              currency: jsonValue(p, endpoint.storeCurrencyPath),
            };
          }),
          diffComponent: StoreDiffComponent,
        })
      }
    ]
  }
];
