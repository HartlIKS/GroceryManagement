import { EndpointDTOTypes, ParameterDTO } from '../../../../models';
import { LIST } from '../../../../../models/base.model';
import { InputSignal, OutputEmitterRef, Signal, signal, WritableSignal } from '@angular/core';
import { FieldTree, form, SchemaOrSchemaFn } from '@angular/forms/signals';
import { ExecForm } from './exec-form-adapter';

export type WithPageInfo<D extends {}> = D & {
  page: number,
  pageSize: number,
  itemCount: number,
};

export abstract class EndpointFormComponent<T extends EndpointDTOTypes[LIST], D extends {} = {}> implements ExecForm<T> {
  abstract readonly api: InputSignal<string>;
  abstract readonly endpoint: InputSignal<T>;
  abstract readonly submit: OutputEmitterRef<{
    headers?: Record<string, string[]>,
    params?: Record<string, string[]>,
  }>;
  protected readonly data: WritableSignal<WithPageInfo<D>>;
  protected readonly form: FieldTree<WithPageInfo<D>>;
  protected abstract readonly htmlForm: Signal<HTMLFormElement>;

  protected constructor(
    defaultValues: D,
    schema: SchemaOrSchemaFn<WithPageInfo<D>>,
  ) {
    this.data = signal({
      pageSize: 100,
      page: 0,
      itemCount: 0,
      ...defaultValues
    });
    this.form = form(this.data, schema, {
      submission: {
        action: async (f) => {
          this.submit.emit(this.aggregate(f().value(), this.endpoint()));
        }
      }
    });
  }

  protected aggregate(sub: WithPageInfo<D>, endpoint: T): {
    headers: Record<string, string[]>,
    params: Record<string, string[]>,
  } {
    const headers = {};
    const params = {};
    EndpointFormComponent.writeParameter(sub.page, endpoint.page, headers, params);
    EndpointFormComponent.writeParameter(sub.pageSize, endpoint.pageSize, headers, params);
    EndpointFormComponent.writeParameter(sub.itemCount, endpoint.itemCount, headers, params);
    return {
      headers,
      params,
    };
  }

  nextPage() {
    this.data.update(({ pageSize, page, itemCount, ...rest }) => ({
      ...rest,
      pageSize,
      page: page + 1,
      itemCount: itemCount + pageSize
    } as D & {
      page: number,
      pageSize: number,
      itemCount: number,
    }));
    this.htmlForm().requestSubmit();
  }

  resetPages() {
    this.data.update((v) => ({
      ...v,
      pageSize: 100,
      page: 0,
      itemCount: 0
    }));
    this.htmlForm().requestSubmit();
  }

  prevPage() {
    this.data.update(({ pageSize, page, itemCount, ...rest }) => ({
      ...rest,
      pageSize,
      page: page - 1,
      itemCount: itemCount - pageSize
    } as D & {
      page: number,
      pageSize: number,
      itemCount: number,
    }));
    this.htmlForm().requestSubmit();
  }

  protected static writeParameter(value: string | number, param: ParameterDTO | undefined, headers: Record<string, string[]>, params: Record<string, string[]>) {
    if (param?.queryParameter) {
      (params[param.queryParameter] ??= []).push(value.toString());
    }
    if (param?.header) {
      (headers[param.header] ??= []).push(value.toString());
    }
  }
}
