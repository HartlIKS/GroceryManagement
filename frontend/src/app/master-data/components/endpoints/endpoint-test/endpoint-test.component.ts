import {
  Component,
  computed,
  inject,
  InjectionToken,
  linkedSignal,
  OnInit,
  Signal,
  signal,
  Type,
  viewChildren
} from '@angular/core';
import { CommonModule, NgComponentOutlet } from '@angular/common';
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
import { httpResource } from '@angular/common/http';
import { ApiService } from '../../../../services';
import { MatCheckbox } from '@angular/material/checkbox';

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

function writeParameter(value: string | number, param: ParameterDTO | undefined, headers: Record<string, string[]>, params: Record<string, string[]>) {
  if(param?.queryParameter) {
    params[param.queryParameter] ??= [];
    params[param.queryParameter].push(value.toString());
  }
  if(param?.header) {
    headers[param.header] ??= [];
    headers[param.header].push(value.toString());
  }
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
    MatIcon,
    MatCheckbox
  ],
  templateUrl: './endpoint-test.component.html',
  styleUrls: ['./endpoint-test.component.css']
})
export class EndpointTestComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly apiService = inject(ApiService);
  private readonly endpointConfig = inject(ENDPOINT_TOKEN);
  protected readonly diffComponent = this.endpointConfig.diffComponent;

  endpointId = signal<string | undefined>(undefined);
  parentUuid = signal<string | undefined>(undefined);

  private readonly endpointResource = this.endpointConfig.endpointService.getEndpoint(this.parentUuid, this.endpointId);
  protected readonly endpoint = computed(() => this.endpointResource.value());

  private readonly pagingInfo = signal({
    page: 0,
    pageSize: 20,
    itemCount: 0,
    direct: false,
  });
  protected readonly pagingForm = form(
    this.pagingInfo,
    schema(() => ({
      page: {type: 'number'},
      pageSize: {type: 'number'},
      itemCount: {type: 'number'},
      direct: {type: 'boolean'},
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
    const parentUuid = this.parentUuid();
    const endpoint = this.endpoint();
    if(parentUuid === undefined || endpoint === undefined) return undefined;
    const reqInfo = this.requestInfo();
    if(!reqInfo.send) return undefined;
    if(!reqInfo.direct) {
      const ret = {
        queryParams: {},
        headers: {}
      };
      writeParameter(reqInfo.page, endpoint?.page, ret.headers, ret.queryParams);
      writeParameter(reqInfo.pageSize, endpoint?.pageSize, ret.headers, ret.queryParams);
      writeParameter(reqInfo.itemCount, endpoint?.itemCount, ret.headers, ret.queryParams);
      return this.apiService.applySpecials({
        url: this.endpointConfig.endpointService.execUrl(parentUuid, endpoint.uuid),
        method: 'POST',
        body: ret,
      });
    }
    const ret = {
      url: endpoint.baseUrl,
      headers: {},
      params: {},
    };
    writeParameter(reqInfo.page, endpoint?.page, ret.headers, ret.params);
    writeParameter(reqInfo.pageSize, endpoint?.pageSize, ret.headers, ret.params);
    writeParameter(reqInfo.itemCount, endpoint?.itemCount, ret.headers, ret.params);
    return ret;
  })
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
      ...p,
      page: p.page + 1,
      itemCount: p.itemCount + p.pageSize,
    }));
    this.requestInfo.update(p => ({...p, send}));
  }

  resetPages() {
    this.pagingInfo.update(p => ({
      ...p,
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
