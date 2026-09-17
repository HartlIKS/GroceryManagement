import {
  Component,
  computed,
  inject,
  InjectionToken,
  OnInit,
  signal,
  Type,
  viewChild,
  viewChildren
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { ActivatedRoute } from '@angular/router';
import { EndpointService, MappingTableService } from '../../../services';
import { EndpointDTOTypes } from '../../../models';
import { MatIcon } from '@angular/material/icon';
import { httpResource, HttpResourceRequest } from '@angular/common/http';
import { ApiService } from '../../../../services';
import { MatCheckbox } from '@angular/material/checkbox';
import { LIST } from '../../../../models/base.model';
import { EndpointFormComponent } from './arg/endpoint-form';
import { DiffAdapter, DiffComponent, DiffStatus } from './diff-adapter';
import { ExecFormAdapter } from './arg/exec-form-adapter';

export type EndpointConfig<E extends EndpointDTOTypes, T extends {} = {}, D extends {} = {}> = {
  formComponent: Type<EndpointFormComponent<E[LIST], D>>,
  endpointService: EndpointService<E>,
  mappingService: MappingTableService,
  toPartials(endpoint: E[LIST], requestResult: string): (Partial<T> & {uuid: string, name: string})[],
  diffComponent: Type<DiffComponent<T>>,
};

export const ENDPOINT_TOKEN = new InjectionToken<EndpointConfig<EndpointDTOTypes>>('EndpointConfig');

@Component({
  selector: 'app-endpoint-test',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIcon,
    MatCheckbox,
    DiffAdapter,
    ExecFormAdapter,
  ],
  templateUrl: './endpoint-test.component.html',
  styleUrls: ['./endpoint-test.component.css']
})
export class EndpointTestComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly apiService = inject(ApiService);
  private readonly endpointConfig = inject(ENDPOINT_TOKEN);

  endpointId = signal<string | undefined>(undefined);
  parentUuid = signal<string | undefined>(undefined);

  private readonly endpointResource = this.endpointConfig.endpointService.get(_ => {
    const api = this.parentUuid();
    if(!api) return undefined;
    const uuid = this.endpointId();
    if(!uuid) return undefined;
    return [api, uuid];
  });
  protected readonly endpoint = computed(() => this.endpointResource.value());

  private readonly form = viewChild.required(ExecFormAdapter);
  protected readonly requestInfo = signal<{
    headers?: Record<string, string[]>;
    params?: Record<string, string[]>;
  } | undefined>(undefined);
  protected readonly direct = signal(true);
  protected readonly requestResource = httpResource.text(({chain}): HttpResourceRequest | undefined => {
    const parentUuid = this.parentUuid();
    const endpoint = chain(this.endpointResource);
    if(parentUuid === undefined || endpoint === undefined) return undefined;
    const reqInfo = this.requestInfo();
    if(!reqInfo) return undefined;
    if(!this.direct()) {
      return {
        url: this.endpointConfig.endpointService.execUrl(parentUuid, endpoint.uuid),
        method: 'POST',
        headers: this.apiService.headers(),
        body: reqInfo,
      };
    }
    return {
      url: endpoint.baseUrl,
      ...reqInfo,
    };
  }, {
    parse: v => {
      return this.endpointConfig.toPartials(this.endpoint()!, v);
    }
  });

  private readonly diffs = viewChildren(DiffAdapter);
  protected readonly diffCounts = computed(() => this.diffs()
    .reduce<Record<DiffStatus | 'total', number>>(
      (a, v) => {
        a.total++;
        a[v.instance()?.status?.() ?? 'loading']++;
        return a;
      },
      {loading: 0, ignored: 0, create: 0, same: 0, different: 0, total: 0}
    )
  )

  ngOnInit(): void {
    this.route.params.subscribe(({id, endpointId}) => {
      this.endpointId.set(endpointId);
      this.parentUuid.set(id);
    });
  }

  nextPage() {
    this.form().nextPage();
  }

  resetPages() {
    this.form().resetPages();
  }

  acceptAll(status?: {
    create?: boolean,
    different?: boolean,
  }) {
    for(const c of this.diffs()) {
      c.instance()?.accept?.(status);
    }
  }
}
