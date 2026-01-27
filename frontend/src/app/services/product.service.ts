import { computed, Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { CreateProductDTO, ListProductDTO, Page } from '../models';

interface ProductState {
  products: ListProductDTO[];
  totalElements: number;
  currentPage: number;
  pageSize: number;
  searchTerm: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly endpoint = '/masterdata/product';

  // Private state signal
  private readonly state = signal<ProductState>({
    products: [],
    totalElements: 0,
    currentPage: 0,
    pageSize: 20,
    searchTerm: ''
  });

  // Public computed signals
  public readonly products = computed(() => this.state().products);
  public readonly totalElements = computed(() => this.state().totalElements);
  public readonly currentPage = computed(() => this.state().currentPage);
  public readonly pageSize = computed(() => this.state().pageSize);
  public readonly searchTerm = computed(() => this.state().searchTerm);
  public readonly loading = computed(() => this.apiService.loading());
  public readonly error = computed(() => this.apiService.error());

  constructor(private apiService: ApiService) {}

  // Get products with caching
  getProducts(name: string = '', page: number = 0, size: number = 20): void {
    this.apiService.get<Page<ListProductDTO>>(this.endpoint, { name, page, size }).subscribe({
      next: (response: Page<ListProductDTO>) => {
        this.state.set({
          products: response.content || [],
          totalElements: response.page.totalElements || 0,
          currentPage: response.page.number || 0,
          pageSize: response.page.size || size,
          searchTerm: name
        });
      },
      error: (error: any) => {
        console.error('Error loading products:', error);
      }
    });
  }

  // Get single product
  getProduct(uuid: string): Observable<ListProductDTO> {
    return this.apiService.getById<ListProductDTO>(this.endpoint, uuid);
  }

  // Create product
  createProduct(product: CreateProductDTO): Observable<ListProductDTO> {
    return this.apiService.post<ListProductDTO>(this.endpoint, product);
  }

  // Update product
  updateProduct(uuid: string, product: CreateProductDTO): Observable<ListProductDTO> {
    return this.apiService.put<ListProductDTO>(this.endpoint, uuid, product);
  }

  // Delete product
  deleteProduct(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

  // Refresh current product list
  refreshProducts(): void {
    const currentState = this.state();
    this.getProducts(currentState.searchTerm, currentState.currentPage, currentState.pageSize);
  }

  // Clear cache
  clearCache(): void {
    this.state.set({
      products: [],
      totalElements: 0,
      currentPage: 0,
      pageSize: 20,
      searchTerm: ''
    });
  }

  // Get product by UUID from cache
  getProductByUuid(uuid: string): ListProductDTO | undefined {
    return this.state().products.find(product => product.uuid === uuid);
  }

  // Update product in cache (optimistic update)
  updateProductInCache(updatedProduct: ListProductDTO): void {
    const currentState = this.state();
    const updatedProducts = currentState.products.map(product =>
      product.uuid === updatedProduct.uuid ? updatedProduct : product
    );

    this.state.set({
      ...currentState,
      products: updatedProducts
    });
  }

  // Remove product from cache (optimistic update)
  removeProductFromCache(uuid: string): void {
    const currentState = this.state();
    const updatedProducts = currentState.products.filter(product => product.uuid !== uuid);

    this.state.set({
      ...currentState,
      products: updatedProducts,
      totalElements: Math.max(0, currentState.totalElements - 1)
    });
  }

  // Add product to cache (optimistic update)
  addProductToCache(newProduct: ListProductDTO): void {
    const currentState = this.state();
    const updatedProducts = [newProduct, ...currentState.products];

    this.state.set({
      ...currentState,
      products: updatedProducts,
      totalElements: currentState.totalElements + 1
    });
  }
}
