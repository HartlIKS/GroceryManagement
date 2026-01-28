import { computed, Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../services';
import { CreateProductGroupDTO, ListProductGroupDTO } from '../models';
import { Page } from '../../models';

interface ProductGroupState {
  productGroups: ListProductGroupDTO[];
  totalElements: number;
  currentPage: number;
  pageSize: number;
  searchTerm: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductGroupService {
  private readonly endpoint = '/productGroups';

  // Private state signal
  private readonly state = signal<ProductGroupState>({
    productGroups: [],
    totalElements: 0,
    currentPage: 0,
    pageSize: 20,
    searchTerm: ''
  });

  // Public computed signals
  public readonly productGroups = computed(() => this.state().productGroups);
  public readonly totalElements = computed(() => this.state().totalElements);
  public readonly currentPage = computed(() => this.state().currentPage);
  public readonly pageSize = computed(() => this.state().pageSize);
  public readonly searchTerm = computed(() => this.state().searchTerm);
  public readonly loading = computed(() => this.apiService.loading());
  public readonly error = computed(() => this.apiService.error());

  constructor(private apiService: ApiService) {}

  // Get product groups with caching
  getProductGroups(name: string = '', page: number = 0, size: number = 20): void {
    this.apiService.get<Page<ListProductGroupDTO>>(this.endpoint, { name, page, size }).subscribe({
      next: (response: Page<ListProductGroupDTO>) => {
        this.state.set({
          productGroups: response.content || [],
          totalElements: response.page.totalElements || 0,
          currentPage: response.page.number || 0,
          pageSize: response.page.size || size,
          searchTerm: name
        });
      },
      error: (error: any) => {
        console.error('Error loading product groups:', error);
      }
    });
  }

  // Get single product group
  getProductGroup(uuid: string): Observable<ListProductGroupDTO> {
    return this.apiService.getById<ListProductGroupDTO>(this.endpoint, uuid);
  }

  // Create product group
  createProductGroup(productGroup: CreateProductGroupDTO): Observable<ListProductGroupDTO> {
    return this.apiService.post<ListProductGroupDTO>(this.endpoint, productGroup);
  }

  // Update product group
  updateProductGroup(uuid: string, productGroup: CreateProductGroupDTO): Observable<ListProductGroupDTO> {
    return this.apiService.put<ListProductGroupDTO>(this.endpoint, uuid, productGroup);
  }

  // Delete product group
  deleteProductGroup(uuid: string): Observable<void> {
    return this.apiService.delete(this.endpoint, uuid);
  }

  // Add product to group
  addProductToGroup(groupUuid: string, productUuid: string): Observable<ListProductGroupDTO> {
    return this.apiService.put<ListProductGroupDTO>(
      this.endpoint,
      `${groupUuid}/${productUuid}`,
      {}
    );
  }

  // Remove product from group
  removeProductFromGroup(groupUuid: string, productUuid: string): Observable<ListProductGroupDTO> {
    return this.apiService.deleteWithData<ListProductGroupDTO>(
      this.endpoint,
      `${groupUuid}/${productUuid}`
    );
  }

  // Refresh current product group list
  refreshProductGroups(): void {
    const currentState = this.state();
    this.getProductGroups(currentState.searchTerm, currentState.currentPage, currentState.pageSize);
  }

  // Clear cache
  clearCache(): void {
    this.state.set({
      productGroups: [],
      totalElements: 0,
      currentPage: 0,
      pageSize: 20,
      searchTerm: ''
    });
  }

  // Get product group by UUID from cache
  getProductGroupByUuid(uuid: string): ListProductGroupDTO | undefined {
    return this.state().productGroups.find(group => group.uuid === uuid);
  }

  // Update product group in cache (optimistic update)
  updateProductGroupInCache(updatedProductGroup: ListProductGroupDTO): void {
    const currentState = this.state();
    const updatedProductGroups = currentState.productGroups.map(group =>
      group.uuid === updatedProductGroup.uuid ? updatedProductGroup : group
    );

    this.state.set({
      ...currentState,
      productGroups: updatedProductGroups
    });
  }

  // Remove product group from cache (optimistic update)
  removeProductGroupFromCache(uuid: string): void {
    const currentState = this.state();
    const updatedProductGroups = currentState.productGroups.filter(group => group.uuid !== uuid);

    this.state.set({
      ...currentState,
      productGroups: updatedProductGroups,
      totalElements: Math.max(0, currentState.totalElements - 1)
    });
  }

  // Add product group to cache (optimistic update)
  addProductGroupToCache(newProductGroup: ListProductGroupDTO): void {
    const currentState = this.state();
    const updatedProductGroups = [newProductGroup, ...currentState.productGroups];

    this.state.set({
      ...currentState,
      productGroups: updatedProductGroups,
      totalElements: currentState.totalElements + 1
    });
  }
}
