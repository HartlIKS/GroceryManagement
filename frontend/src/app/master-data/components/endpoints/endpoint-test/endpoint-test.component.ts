import {
  Component,
  computed,
  inject,
  InjectionToken,
  linkedSignal,
  OnInit, Signal,
  signal,
  Type,
  viewChildren
} from '@angular/core';
import { CommonModule, NgComponentOutlet } from '@angular/common';
import { HttpHeaders, HttpParams, httpResource } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { ActivatedRoute } from '@angular/router';
import { EndpointService } from '../../../services';
import { EndpointDTO, ParameterDTO } from '../../../models';
import { form, FormField, FormRoot, schema } from '@angular/forms/signals';
import { MatIcon } from '@angular/material/icon';

export type DiffStatus = 'loading' | 'ignored' | 'create' | 'same' | 'different';

export interface DiffComponent {
  readonly status: Signal<DiffStatus>;
  accept(status?: {
    create?: boolean,
    different?: boolean,
  }): any;
}

export type EndpointConfig<E extends EndpointDTO, T extends {uuid: string, name: string}> = {
  endpointService: EndpointService<E, any>;
  toPartials: (endpoint: E, requestResult: string) => (Partial<T> & {uuid: string})[];
  diffComponent: Type<DiffComponent>;
};

export const ENDPOINT_TOKEN = new InjectionToken<EndpointConfig<EndpointDTO, {uuid: string, name: string}>>('EndpointConfig');

function writeParameter(value: string | number, param: ParameterDTO, headers: HttpHeaders, params: HttpParams) {
  if(param.queryParameter) params = params.append(param.queryParameter, value);
  if(param.header) headers = headers.append(param.header, value.toString());
  return [headers, params] as const;
}

@Component({
  selector: 'app-endpoint-test',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInput,
    MatProgressSpinner,
    FormRoot,
    FormField,
    MatIcon
  ],
  templateUrl: './endpoint-test.component.html',
  styleUrls: ['./endpoint-test.component.css']
})
export class EndpointTestComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly endpointConfig = inject(ENDPOINT_TOKEN);
  protected readonly diffComponent = this.endpointConfig.diffComponent;

  endpointId = signal<string | undefined>(undefined);
  parentUuid = signal<string | undefined>(undefined);

  private readonly endpointResource = this.endpointConfig.endpointService.getEndpoint(this.parentUuid, this.endpointId);
  protected readonly endpoint = computed(() => this.endpointResource.value());

  private readonly pagingInfo = signal({
    page: 0,
    pageSize: 20,
    itemCount: 0
  });
  protected readonly pagingForm = form(
    this.pagingInfo,
    schema(() => ({
      page: {type: 'number'},
      pageSize: {type: 'number'},
      itemCount: {type: 'number'}
    })),
    {
      submission: {
        action: async () => {
          this.requestInfo.update(p => ({...p, send: true}));
        },
      }
    }
  );
  protected readonly requestInfo = linkedSignal(() => ({...this.pagingInfo(), send: false}));
  protected readonly requestResource = httpResource.text(() => {
    const reqInfo = this.requestInfo();
    if(!reqInfo.send) return undefined;
    const endpoint = this.endpoint();
    if(!endpoint) return undefined;
    let headers = new HttpHeaders();
    let queryParams = new HttpParams();
    [headers, queryParams] = writeParameter(reqInfo.page, endpoint.page, headers, queryParams);
    [headers, queryParams] = writeParameter(reqInfo.pageSize, endpoint.pageSize, headers, queryParams);
    [headers, queryParams] = writeParameter(reqInfo.itemCount, endpoint.itemCount, headers, queryParams);
    return {
      url: endpoint.baseUrl,
      headers: headers,
      params: queryParams
    };
  });
  protected readonly parsedResponse = linkedSignal(() => {
    const response = this.requestResource.value();
    if(response === undefined) return undefined;
    const endpoint = this.endpoint();
    if(endpoint === undefined) return undefined;
    return this.endpointConfig.toPartials(endpoint, response);
  })

  private readonly diffs = viewChildren<NgComponentOutlet<DiffComponent>>(NgComponentOutlet);
  protected readonly diffCounts = computed(() => this.diffs()
    .reduce<Record<DiffStatus | 'total', number>>(
      (a, v) => {
        a.total++;
        a[v.componentInstance?.status() ?? 'loading']++;
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
    const send = this.requestInfo().send;
    this.pagingInfo.update(p => ({
      page: p.page + 1,
      itemCount: p.itemCount + p.pageSize,
      pageSize: p.pageSize
    }));
    this.requestInfo.update(p => ({...p, send}));
  }

  resetPages() {
    this.pagingInfo.update(p => ({
      page: 0,
      pageSize: p.pageSize,
      itemCount: 0
    }));
  }

  acceptAll(status?: {
    create?: boolean,
    different?: boolean,
  }) {
    for(const c of this.diffs()) {
      c.componentInstance?.accept(status);
    }
  }
}
